package MVC;

class Task {
    private final TaskType taskType;
    private final Object[] value;

    /**
     * Creates a new {@code Task} object.
     * Requires: TaskType of LOAD_WORLD means {@code arg} must be a filePath String object
     * <br>TaskType =f Load_CRITTER_RANDOMLY
     * <br>TaskType of SELECT_CRITTER means {@code} arg must be a ReadOnlyCritter object
     * <br> TaskType of ANYTHING ELSE means {@code} arg must be null
     */
    public Task(TaskType taskType,Object... arg) {
        if(taskType == TaskType.LOAD_WORLD)
            assert arg.length == 1 && arg[0] instanceof String;
        else assert arg.length == 0;

        this.taskType = taskType;
        value = arg;
    }

    /**
     * Returns the appropriate value(s) associated with this task in an Object array according to {@link TaskType}
     */
    public Object[] getValue(){return value;}

    public TaskType getType(){return taskType;}

    public enum TaskType {
        /** No values associated. Creates a brand-new world */
        CREATE_NEW_WORLD, //needs no arg
        /** Object[] is length 1 and contains a String object representing a filePath */
        LOAD_WORLD, //needs String arg (filePath)
        /** No values associated */
        STOP_RUNNING, //needs no arg
        /** No values associated */
        RUN_CONTINUOUSLY, //needs no arg
        /** No values associated */
        STEP; //needs no arg
    }
}
