package fgbml.multitask;

import java.util.ArrayList;

public class TaskManager {
	// ************************************************************
	ArrayList<Task> tasks = new ArrayList<>();

	int numMigration;

	// ************************************************************
	public TaskManager() {}

	// ************************************************************

	public void setNumMigration(int numMigration) {
		this.numMigration = numMigration;
	}

	public int getNumMigration() {
		return this.numMigration;
	}

	public void addTask(Task task) {
		tasks.add(task);
	}

	public Task getTask(int index) {
		return this.tasks.get(index);
	}

	public int getTaskNum() {
		return tasks.size();
	}
}
