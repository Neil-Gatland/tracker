package com.devoteam.tracker.model;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class JobType {
	private String jobType;
	private String projectRequestor;
	private String projectManager;
	private String actingCustomer;
	private Date lastUpdatedDate;
	private String lastUpdatedBy;
	private String projectRequestorEmail;
	private String projectManagerEmail;
	private String redundant;
	private String bypassCompletionReport;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
	
	public JobType(String jobType, String projectRequestor,
			Date lastUpdatedDate, String lastUpdatedBy,
			String projectManager, String actingCustomer,
			String projectRequestorEmail, String projectManagerEmail,
			String redundant, String bypassCompletionReport) {
		this.jobType = jobType;
		this.projectRequestor = projectRequestor;
		this.lastUpdatedDate = lastUpdatedDate;
		this.lastUpdatedBy = lastUpdatedBy;
		this.projectManager = projectManager;
		this.actingCustomer = actingCustomer;
		this.projectRequestorEmail = projectRequestorEmail;
		this.projectManagerEmail = projectManagerEmail;
		this.redundant = redundant;
		this.bypassCompletionReport = bypassCompletionReport;
	}

	public String getJobType() {
		return jobType;
	}

	public String getProjectRequestor() {
		return projectRequestor;
	}

	public String getProjectManager() {
		return projectManager;
	}

	public String getActingCustomer() {
		return actingCustomer;
	}

	public Date getLastUpdatedDate() {
		return lastUpdatedDate;
	}
	
	public String getLastUpdatedDateString() {
		return lastUpdatedDate==null?"":dateFormatter.format(lastUpdatedDate);
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}
	
	public String getProjectRequestorEmail() {
		return projectRequestorEmail;
	}
	
	public String getProjectManagerEmail() {
		return projectManagerEmail;
	}
	
	public String getRedundant() {
		return redundant;
	}
	
	public String getBypassCompletionReport() {
		return bypassCompletionReport;
	}
	
	public String[] getListValueArray() {
		String[] values = {this.getJobType(), this.getProjectRequestor(),
				this.getProjectRequestorEmail(), this.getProjectManager(), 
				this.getProjectManagerEmail(), this.getActingCustomer(),	
			this.getLastUpdatedBy(), this.getLastUpdatedDateString(),
			this.getRedundant(), this.bypassCompletionReport};
		return values;
	}
}
