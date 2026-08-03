import {
    Box,
    Button,
    Dialog,
    DialogActions,
    DialogContent,
    DialogTitle,
    MenuItem,
    Paper,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    TextField,
    Typography,
    CircularProgress,
} from "@mui/material";

import { useEffect, useState } from "react";

import {
    createTask,
    getAllTasks,
    updateTask,
    deleteTask,
} from "../services/taskService";


function Tasks() {

    // Dialog
    const [open, setOpen] = useState(false);

    // Form Fields
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [status, setStatus] = useState("TODO");
    const [priority, setPriority] = useState("MEDIUM");
    const [dueDate, setDueDate] = useState("");

    // Tasks
    const [tasks, setTasks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [editingTaskId, setEditingTaskId] = useState(null);

    const [search, setSearch] = useState("");
    const [statusFilter, setStatusFilter] = useState("ALL");
    const [priorityFilter, setPriorityFilter] = useState("ALL");
    const [sortBy, setSortBy] = useState("NEWEST");

    useEffect(() => {
        loadTasks();
    }, []);

    const loadTasks = async () => {
        setLoading(true);
        try {

            const response = await getAllTasks();

            setTasks(response);
            setLoading(false);
        } catch (error) {

            console.error(error);
            setLoading(false);

        }
    };

    const resetForm = () => {

        setEditingTaskId(null);

        setTitle("");
        setDescription("");
        setStatus("TODO");
        setPriority("MEDIUM");
        setDueDate("");

        setOpen(false);

    };
    const handleOpenCreateDialog = () => {

        resetForm();

        // resetForm() closes dialog,
        // so reopen it immediately.
        setOpen(true);
    };

    const handleSaveTask = async () => {

        try {

            const task = {
                title,
                description,
                status,
                priority,
                dueDate,
            };

            if (editingTaskId) {

                await updateTask(editingTaskId, task);

            } else {

                await createTask(task);

            }

            resetForm();

            await loadTasks();

        } catch (error) {

            console.error(error);

        }

    };

    const filteredTasks = tasks.filter((task) => {

        const matchesSearch =
            task.title.toLowerCase().includes(search.toLowerCase());

        const matchesStatus =
            statusFilter === "ALL" || task.status === statusFilter;

        const matchesPriority =
            priorityFilter === "ALL" || task.priority === priorityFilter;

        return (
            matchesSearch &&
            matchesStatus &&
            matchesPriority
        );

    });

    const sortedTasks = [...filteredTasks];

    if (sortBy === "NEWEST") {

        sortedTasks.sort((a, b) => b.id - a.id);

    }
    else if (sortBy === "OLDEST") {

        sortedTasks.sort((a, b) => a.id - b.id);

    }
    else if (sortBy === "DUE_DATE") {

        sortedTasks.sort(
            (a, b) => new Date(a.dueDate) - new Date(b.dueDate)
        );

    }

    const handleDeleteTask = async (id) => {

        const confirmed = window.confirm(
            "Are you sure you want to delete this task?"
        );

        if (!confirmed) {
            return;
        }

        try {

            await deleteTask(id);

            await loadTasks();

        } catch (error) {

            console.error(error);

        }

    };

    if (loading) {

        return (

            <Box
                display="flex"
                justifyContent="center"
                mt={5}
            >
                <CircularProgress />
            </Box>

        );

    }

    return (
        <>
            <Box
                sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                    mb: 3,
                }}
            >
                <Typography variant="h4">
                    Tasks
                </Typography>

                <Button
                    variant="contained"
                    onClick={handleOpenCreateDialog}
                >
                    Create Task
                </Button>
            </Box>

            <Box
                sx={{
                    display: "flex",
                    gap: 2,
                    mb: 3,
                }}
            >

                <TextField
                    label="Search Tasks"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    sx={{ flex: 2 }}
                />

                <TextField
                    select
                    label="Status"
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                    sx={{ flex: 1 }}
                >

                    <MenuItem value="ALL">
                        ALL
                    </MenuItem>

                    <MenuItem value="TODO">
                        TODO
                    </MenuItem>

                    <MenuItem value="IN_PROGRESS">
                        IN_PROGRESS
                    </MenuItem>

                    <MenuItem value="DONE">
                        DONE
                    </MenuItem>

                </TextField>

                <TextField
                    select
                    label="Priority"
                    value={priorityFilter}
                    onChange={(e) => setPriorityFilter(e.target.value)}
                    sx={{ flex: 1 }}
                >

                    <MenuItem value="ALL">
                        ALL
                    </MenuItem>

                    <MenuItem value="LOW">
                        LOW
                    </MenuItem>

                    <MenuItem value="MEDIUM">
                        MEDIUM
                    </MenuItem>

                    <MenuItem value="HIGH">
                        HIGH
                    </MenuItem>

                </TextField>

                <TextField
                    select
                    label="Sort By"
                    value={sortBy}
                    onChange={(e) => setSortBy(e.target.value)}
                    sx={{ flex: 1 }}
                >

                    <MenuItem value="NEWEST">
                        Newest
                    </MenuItem>

                    <MenuItem value="OLDEST">
                        Oldest
                    </MenuItem>

                    <MenuItem value="DUE_DATE">
                        Due Date
                    </MenuItem>

                </TextField>

            </Box>

            {
                sortedTasks.length === 0 ? (

                    <Paper
                        sx={{
                            p: 5,
                            textAlign: "center"
                        }}
                    >

                        <Typography variant="h6">

                            No Tasks Found

                        </Typography>

                        <Typography color="text.secondary">

                            Create your first task.

                        </Typography>

                    </Paper>

                ) : (

                    <TableContainer component={Paper}>

                        <Table>

                            <TableHead>

                                <TableRow>

                                    <TableCell>Title</TableCell>

                                    <TableCell>Status</TableCell>

                                    <TableCell>Priority</TableCell>

                                    <TableCell>Actions</TableCell>

                                </TableRow>

                            </TableHead>

                            <TableBody>

                                {sortedTasks.map((task) => (
                                    <TableRow key={task.id}>

                                        <TableCell>
                                            {task.title}
                                        </TableCell>

                                        <TableCell>
                                            {task.status}
                                        </TableCell>

                                        <TableCell>
                                            {task.priority}
                                        </TableCell>

                                        <TableCell>

                                            <Button
                                                size="small"
                                                onClick={() => {
                                                    setEditingTaskId(task.id);
                                                    setTitle(task.title);
                                                    setDescription(task.description);
                                                    setStatus(task.status);
                                                    setPriority(task.priority);
                                                    setDueDate(task.dueDate);
                                                    setOpen(true);
                                                }}
                                            >
                                                Edit
                                            </Button>

                                            <Button
                                                size="small"
                                                color="error"
                                                onClick={() => handleDeleteTask(task.id)}
                                            >
                                                Delete
                                            </Button>

                                        </TableCell>

                                    </TableRow>

                                ))}

                            </TableBody>

                        </Table>

                    </TableContainer>
                )

            }

            <Dialog
                open={open}
                onClose={resetForm}
                fullWidth
            >

                <DialogTitle>

                    {editingTaskId ? "Edit Task" : "Create Task"}

                </DialogTitle>

                <DialogContent
                    sx={{
                        display: "flex",
                        flexDirection: "column",
                        gap: 2,
                        mt: 1,
                    }}
                >

                    <TextField
                        label="Title"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                    />

                    <TextField
                        label="Description"
                        multiline
                        rows={3}
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                    />

                    <TextField
                        select
                        label="Status"
                        value={status}
                        onChange={(e) => setStatus(e.target.value)}
                    >
                        <MenuItem value="TODO">
                            TODO
                        </MenuItem>

                        <MenuItem value="IN_PROGRESS">
                            IN_PROGRESS
                        </MenuItem>

                        <MenuItem value="DONE">
                            DONE
                        </MenuItem>

                    </TextField>

                    <TextField
                        select
                        label="Priority"
                        value={priority}
                        onChange={(e) => setPriority(e.target.value)}
                    >
                        <MenuItem value="LOW">
                            LOW
                        </MenuItem>

                        <MenuItem value="MEDIUM">
                            MEDIUM
                        </MenuItem>

                        <MenuItem value="HIGH">
                            HIGH
                        </MenuItem>

                    </TextField>

                    <TextField
                        label="Due Date"
                        type="date"
                        value={dueDate}
                        onChange={(e) => setDueDate(e.target.value)}
                        slotProps={{
                            inputLabel: {
                                shrink: true,
                            },
                        }}
                    />

                </DialogContent>

                <DialogActions>

                    <Button onClick={resetForm}>
                        Cancel
                    </Button>

                    <Button
                        variant="contained"
                        onClick={handleSaveTask}
                    >
                        {editingTaskId ? "Update" : "Save"}
                    </Button>

                </DialogActions>

            </Dialog>

        </>
    );
}

export default Tasks;