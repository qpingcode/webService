package com.neusoft.common.mybatis.interceptor;


import com.neusoft.common.mybatis.dialect.Dialect;
import com.neusoft.common.mybatis.dialect.OracleDialect;
import com.neusoft.common.util.ReflectHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * 
 * @author chen_dong
 * 
 * @description 然后就是实现mybatis提供的拦截器接口，编写我们自己的分页实现，原理就是拦截底层JDBC操作相关的Statement对象，
 *              把前端的分页参数如当前记录索引和每页大小通过拦截器注入到sql语句中
 *              ，即在sql执行之前通过分页参数重新生成分页sql,而具体的分页sql实现是分离到Dialect接口中去。
 * 
 * 
 */
@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class }) })
public class PaginationInterceptor implements Interceptor {

	private final static Log log = LogFactory.getLog(PaginationInterceptor.class);

	public Object intercept(Invocation invocation) throws Throwable {
		
		RoutingStatementHandler statementHandler = (RoutingStatementHandler)invocation.getTarget();
		BoundSql boundSql = statementHandler.getBoundSql();
		MetaObject metaStatementHandler = SystemMetaObject.forObject(statementHandler);
		
        BaseStatementHandler delegate = (BaseStatementHandler) ReflectHelper.getValueByFieldName(statementHandler, "delegate");  
        MappedStatement mappedStatement = (MappedStatement) ReflectHelper.getValueByFieldName(delegate, "mappedStatement");
        Connection connection = (Connection)invocation.getArgs()[0];
        
		//获取当前准备执行的行边界
		RowBounds rowBounds = (RowBounds) metaStatementHandler.getValue("delegate.rowBounds");
		
		//判断是否设置分页
		if (rowBounds == null || rowBounds == RowBounds.DEFAULT) {
			return invocation.proceed();
		}

		Configuration configuration = (Configuration) metaStatementHandler.getValue("delegate.configuration");
		Dialect.Type databaseType = null;
		//获取当前的数据库方言
		try {

			databaseType = Dialect.Type.valueOf(configuration.getVariables()
					.getProperty("dialect").toUpperCase());

		} catch (Exception e) {
			// ignore
			throw e;
		}

		if (databaseType == null) {
			throw new RuntimeException(
					"the value of the dialect property in configuration.xml is not defined : "
							+ configuration.getVariables().getProperty(
									"dialect"));
		}

		Dialect dialect = null;
		//判断数据库方言
		switch (databaseType) {
		
		case ORACLE:
			dialect = new OracleDialect();
			
		}
		//原始sql
		String originalSql = (String) metaStatementHandler
				.getValue("delegate.boundSql.sql");
		//统计总数
		int count = getCount(connection, boundSql, configuration, mappedStatement);
		Object obj = boundSql.getParameterObject();
		if ( null != obj && obj instanceof Map){
			Map parameterObject = (Map)boundSql.getParameterObject();
			parameterObject.put("_dataCount", count);
		}else{
			throw new NullPointerException("service中getGridData的condition参数不能为空！");
		}
		//设置拼接好的分页sql
		metaStatementHandler.setValue("delegate.boundSql.sql", dialect
				.getPageSql(originalSql, rowBounds.getOffset(),
						rowBounds.getLimit()));
		
		//重置行边界
		metaStatementHandler.setValue("delegate.rowBounds.offset",
				RowBounds.NO_ROW_OFFSET);

		metaStatementHandler.setValue("delegate.rowBounds.limit",
				RowBounds.NO_ROW_LIMIT);

		if (log.isDebugEnabled()) {
			log.debug("生成分页SQL : " + boundSql.getSql());
		}

		return invocation.proceed();

	}

	public Object plugin(Object target) {

		return Plugin.wrap(target, this);

	}

	public void setProperties(Properties properties) {

	}
	
	private int getCount(Connection connection,BoundSql boundSql,Configuration configuration,MappedStatement mappedStatement) throws SQLException {
		
		 String countSql = "select count(0) from ( " + boundSql.getSql()+ " )"; //记录统计
        PreparedStatement countStmt = connection.prepareStatement(countSql);
        BoundSql countBS = new BoundSql(configuration,countSql,boundSql.getParameterMappings(),boundSql.getParameterObject());  
        setParameters(countStmt,mappedStatement,countBS,boundSql.getParameterObject());  
        ResultSet rs = countStmt.executeQuery();
        int count = 0;  
        if (rs.next()) {  
            count = rs.getInt(1);  
        }  
        rs.close();  
        countStmt.close();
        
        return count;
        
	}
	
	private void setParameters(PreparedStatement ps,MappedStatement mappedStatement,BoundSql boundSql,Object parameterObject) throws SQLException {
       ErrorContext.instance().activity("setting parameters").object(mappedStatement.getParameterMap().getId());  
       List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
       if (parameterMappings != null) {  
           Configuration configuration = mappedStatement.getConfiguration();  
           TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();  
           MetaObject metaObject = parameterObject == null ? null: configuration.newMetaObject(parameterObject);  
           for (int i = 0; i < parameterMappings.size(); i++) {  
               ParameterMapping parameterMapping = parameterMappings.get(i);  
               if (parameterMapping.getMode() != ParameterMode.OUT) {  
                   Object value;
                   String propertyName = parameterMapping.getProperty();
                   PropertyTokenizer prop = new PropertyTokenizer(propertyName);  
                   if (parameterObject == null) {  
                       value = null;  
                   } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {  
                       value = parameterObject;  
                   } else if (boundSql.hasAdditionalParameter(propertyName)) {  
                       value = boundSql.getAdditionalParameter(propertyName);  
                   } else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX)&& boundSql.hasAdditionalParameter(prop.getName())) {  
                       value = boundSql.getAdditionalParameter(prop.getName());  
                       if (value != null) {  
                           value = configuration.newMetaObject(value).getValue(propertyName.substring(prop.getName().length()));  
                       }  
                   } else {  
                       value = metaObject == null ? null : metaObject.getValue(propertyName);  
                   }  
                   TypeHandler typeHandler = parameterMapping.getTypeHandler();  
                   if (typeHandler == null) {  
                       throw new ExecutorException("There was no TypeHandler found for parameter "+ propertyName + " of statement "+ mappedStatement.getId());  
                   }  
                   typeHandler.setParameter(ps, i + 1, value, parameterMapping.getJdbcType());  
               }  
           }  
       }  
   } 

}

