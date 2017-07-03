package com.stmt.query;

/**
 * Created by HienNguyen on 7/3/2017.
 */
public class Le extends BaseComparison {

	public Le(String columnName, boolean isNumber, Object value) {
		super(columnName, isNumber, value);
	}

	@Override
	public StringBuilder appendOperation(StringBuilder sb) {
		sb.append("<= ");
		return sb;
	}
}