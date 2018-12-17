package com.technologies.highstreet.mediatorlib.data;

import java.util.ArrayList;
import java.util.List;

public class DataTable {

	private final List<String> columns;
	private final List<DataRow> rows;
	private final String baseOID;
	private boolean autoCreateRow;

	public String getBase() {
		return this.getBase();
	}

	public DataTable() {
		this("", new String[] {}, 0, true);
	}

	public DataTable(String base, String[] cols, int numRows, boolean autoCreate) {
		this.autoCreateRow = autoCreate;
		this.baseOID = base;
		this.columns = new ArrayList<String>();
		if (cols != null) {
			for (String c : cols)
				this.columns.add(c);
		}
		this.rows = new ArrayList<DataRow>();
		while (numRows-- > 0)
			this.rows.add(null);
	}

	public DataTable(String base, String[] cols, String[] rows) {
		this.baseOID = base;
		this.columns = new ArrayList<String>();
		if (cols != null) {
			for (String c : cols)
				this.columns.add(c);
		}
		this.rows = new ArrayList<DataRow>();
		if(rows!=null)
		{
			for (int i = 0; i < rows.length; i++)
				this.rows.add(new DataRow(rows[i]));
		}
		else
			this.autoCreateRow=true;
	}

	public void AddColumn(String colName) {
		this.columns.add(colName);
	}

	public DataRow AddRow(String key,Object[] values) {
		DataRow row = new DataRow(key,values);
		this.rows.add(row);
		return row;
	}

	public Object getValueAt(String col, int row) {
		return getValueAt(this.columns.indexOf(col), row);
	}

	public Object getValueAt(int col, int row) {
		if (row < rows.size())
			return rows.get(row).getValueAt(col);
		return null;
	}

	public boolean setValueAt(String col, int row, Object value) {
		return this.setValueAt(this.columns.indexOf(col), row, value);
	}


	public boolean setValueAt(int col, int row, Object value) {
		return this.setValueAt(col, row,String.format("%d",row), value);

	}
	public boolean setValueAt(int col, int xrow, String rowIndex, Object value) {
		DataRow r = this.getRow(rowIndex);
		if(r==null && this.autoCreateRow)
		{
			r=this.AddRow(rowIndex,null);
		}
		if(r!=null)
			return r.setValueAt(col, value);
		else
			System.out.println("row to big");
		return false;
	}
	private DataRow getRow(int row) {
		return this.getRow(String.format("%d", row));
	}

	private DataRow getRow(String key) {
		for(DataRow r : this.rows)
		{
			if(r.getKey().equals(key))
				return r;
		}
		return null;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("index\t");
		for (String col : this.columns)
			sb.append(col + "\t");
		sb.append("\n");
		String[] cols=new String[this.columns.size()];
		for(int i=0;i<cols.length;i++)
			cols[i]=String.valueOf(i);
		for (DataRow row : this.rows)
			sb.append(row.getKey()+"\t"+row.toString(cols,"\t") + "\n");
		return sb.toString();
	}

	public List<DataRow> getRows() {
		return this.rows;
	}




}
