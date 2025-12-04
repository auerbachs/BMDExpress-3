package com.sciome.bmdexpress2.shared.eventbus.project;

import java.util.List;
import com.sciome.bmdexpress2.util.ExperimentDescriptionParser;
import com.sciome.bmdexpress2.shared.eventbus.BMDExpressEventBase;

public class ShowValidationErrorEvent extends BMDExpressEventBase<ShowValidationErrorEvent.ValidationErrorData>
{
	public static class ValidationErrorData {
		private final List<ExperimentDescriptionParser.ValidationIssue> issues;
		private final String filename;

		public ValidationErrorData(List<ExperimentDescriptionParser.ValidationIssue> issues, String filename) {
			this.issues = issues;
			this.filename = filename;
		}

		public List<ExperimentDescriptionParser.ValidationIssue> getIssues() {
			return issues;
		}

		public String getFilename() {
			return filename;
		}
	}

	public ShowValidationErrorEvent(List<ExperimentDescriptionParser.ValidationIssue> issues, String filename)
	{
		super(new ValidationErrorData(issues, filename));
	}
}
