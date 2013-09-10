	package com.cds.fitnesse.utils;
	
	public class SubmittedJob {
		

		private String jobNumber;
		private String jobUser;
		private String jobName;
		private String submitMessage;
		
		public SubmittedJob(){
			jobNumber = jobUser = jobName = "";
		}
		public SubmittedJob(String number, String user, String name){
			this.jobNumber = number;
			this.jobUser = user;
			this.jobName = name;
			
		}
		public String getJobNumber() {
			return jobNumber;
		}
		public void setJobNumber(String jobNumber) {
			this.jobNumber = jobNumber;
		}
		public String getJobUser() {
			return jobUser;
		}
		public void setJobUser(String jobUser) {
			this.jobUser = jobUser;
		}
		public String getJobName() {
			return jobName;
		}
		public void setJobName(String jobName) {
			this.jobName = jobName;
		}
		public String getSubmitMessage() {
			return submitMessage;
		}
		public void setSubmitMessage(String submitMessage) {
			this.submitMessage = submitMessage;
		}		
	}