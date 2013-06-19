package com.jbrunton.todolist.data;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.jbrunton.todolist.models.Task;

public class TasksDataSource {

	// Database fields
	private SQLiteDatabase database;
	private TodoListSQLiteOpenHelper dbHelper;
	private String[] allColumns = {
			TodoListSQLiteOpenHelper.COLUMN_ID,
			TodoListSQLiteOpenHelper.COLUMN_TITLE,
			TodoListSQLiteOpenHelper.COLUMN_DETAILS,
			TodoListSQLiteOpenHelper.COLUMN_COMPLETE,
			TodoListSQLiteOpenHelper.COLUMN_DUE_DATE};

	public TasksDataSource(Context context) {
		dbHelper = new TodoListSQLiteOpenHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
		
		Calendar rightNow = Calendar.getInstance(); // this defaults to today's date 
		
		// urgh, test data in the wrong place
		// not sure if this is the best way, but it appears to work for the UK locale . . .
		if (getAllTasks().size() == 0) {
			String dateFormat = DateFormat.getDateInstance().format(rightNow.getTime());
			createTask("Task 1", dateFormat );
			rightNow.roll(Calendar.WEEK_OF_MONTH, 1);
			dateFormat = DateFormat.getDateInstance().format(rightNow.getTime());
			createTask("Task 2", dateFormat);
			rightNow.roll(Calendar.WEEK_OF_MONTH, 2);
			dateFormat = DateFormat.getDateInstance().format(rightNow.getTime());
			createTask("Task 3", dateFormat);
		}
	}

	public void close() {
		dbHelper.close();
	}

	public Task createTask(String title, String dueDate) {
		ContentValues values = new ContentValues();
		values.put(TodoListSQLiteOpenHelper.COLUMN_TITLE, title);
		values.put(TodoListSQLiteOpenHelper.COLUMN_COMPLETE, 0);
		values.put(TodoListSQLiteOpenHelper.COLUMN_DUE_DATE, dueDate);
		
		long insertId = database.insert(TodoListSQLiteOpenHelper.TABLE_TASKS,
				null, values);
		Cursor cursor = database.query(TodoListSQLiteOpenHelper.TABLE_TASKS,
				allColumns, TodoListSQLiteOpenHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		Task task = cursorToTask(cursor);
		cursor.close();
		return task;
	}

	public void deleteTask(Task task) {
		long id = task.getId();
		System.out.println("Task deleted with id: " + id);
		database.delete(TodoListSQLiteOpenHelper.TABLE_TASKS, TodoListSQLiteOpenHelper.COLUMN_ID
				+ " = " + id, null);
	}
	
	public void saveTask(Task task) {
		long id = task.getId();
		
		ContentValues values = new ContentValues();
		values.put(TodoListSQLiteOpenHelper.COLUMN_TITLE, task.getTitle());
		values.put(TodoListSQLiteOpenHelper.COLUMN_DETAILS, task.getDetails());
		values.put(TodoListSQLiteOpenHelper.COLUMN_COMPLETE, task.getComplete() ? 1 : 0);
		values.put(TodoListSQLiteOpenHelper.COLUMN_DETAILS, task.getDueDate());
		
		System.out.println("Task saved with id: " + id);
		database.update(TodoListSQLiteOpenHelper.TABLE_TASKS, values,
				TodoListSQLiteOpenHelper.COLUMN_ID + " = " + id, null);
	}
	
	public Task find(long id) {
		for (Task task : getAllTasks()) {
			if (task.getId() == id) {
				return task;
			}
		}
		return null;
	}

	public List<Task> getAllTasks() {
		List<Task> tasks = new ArrayList<Task>();

		Cursor cursor = database.query(TodoListSQLiteOpenHelper.TABLE_TASKS,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Task task = cursorToTask(cursor);
			tasks.add(task);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return tasks;
	}

	private Task cursorToTask(Cursor cursor) {
		Task task = new Task();
		task.setId(cursor.getLong(0));
		task.setTitle(cursor.getString(1));
		task.setDetails(cursor.getString(2));
		task.setComplete(cursor.getInt(3) == 0 ? false : true);
		task.setDueDate(cursor.getString(4));
		return task;
	}
} 
