package com.jadaptive.entity.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSource {

	static Logger log = LoggerFactory.getLogger(DataSource.class);
	static EntityDataSource dataSource = null;
	
	public static EntityDataSource getDataSource() throws DataSourceException {
		if(dataSource==null) {
			throw new DataSourceException("No data source has been initialised");
		}
		return dataSource;
	}

	public static void init(EntityDataSource dataSource) throws DataSourceException {
		if(DataSource.dataSource!=null) {
			throw new DataSourceException("Data source has already been initialised");
		}
		if(log.isInfoEnabled()) {
			log.info("Initilising data source {}", dataSource.getName());
		}
		DataSource.dataSource = dataSource;
	}
	
	public static void close(EntityDataSource dataSource) throws DataSourceException {
		if(DataSource.dataSource==null) {
			throw new DataSourceException("No data source has been initialised");
		}
		if(!dataSource.equals(DataSource.dataSource)) {
			throw new DataSourceException("The data source initialied is not the data source being closed!");
		}
		DataSource.dataSource = null;
	}
}
