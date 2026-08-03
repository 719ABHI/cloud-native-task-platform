import {
    Box,
    Card,
    CardContent,
    Typography,
} from "@mui/material";

import { useEffect, useState } from "react";
import { getUser } from "../utils/token";
import { getAllTasks } from "../services/taskService";

function Dashboard() {

    const [tasks, setTasks] = useState([]);
    const user = getUser();

    useEffect(() => {

        loadTasks();

    }, []);

    const loadTasks = async () => {

        try {

            const response = await getAllTasks();

            setTasks(response);

        } catch (error) {

            console.error(error);

        }

    };

    const totalTasks = tasks.length;

    const todoTasks = tasks.filter(
        task => task.status === "TODO"
    ).length;

    const inProgressTasks = tasks.filter(
        task => task.status === "IN_PROGRESS"
    ).length;

    const doneTasks = tasks.filter(
        task => task.status === "DONE"
    ).length;

    const highPriorityTasks = tasks.filter(
        task => task.priority === "HIGH"
    ).length;

    const overdueTasks = tasks.filter(task =>

        task.status !== "DONE" &&

        new Date(task.dueDate) < new Date()

    ).length;

    return (

        <>

            <Box mb={3}>

                <Typography variant="h4">

                    Welcome, {user?.username} 👋

                </Typography>

                <Typography
                    variant="body1"
                    color="text.secondary"
                >
                    Here's an overview of your tasks.
                </Typography>

            </Box>

            <Box
                sx={{
                    display: "grid",
                    gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
                    gap: 2,
                    mt: 3,
                }}
            >

                <Card>

                    <CardContent>

                        <Typography variant="h6">
                            Total Tasks
                        </Typography>

                        <Typography variant="h3">
                            {totalTasks}
                        </Typography>

                    </CardContent>

                </Card>

                <Card>

                    <CardContent>

                        <Typography variant="h6">
                            Todo
                        </Typography>

                        <Typography variant="h3">
                            {todoTasks}
                        </Typography>

                    </CardContent>

                </Card>

                <Card>

                    <CardContent>

                        <Typography variant="h6">
                            In Progress
                        </Typography>

                        <Typography variant="h3">
                            {inProgressTasks}
                        </Typography>

                    </CardContent>

                </Card>

                <Card>

                    <CardContent>

                        <Typography variant="h6">
                            Done
                        </Typography>

                        <Typography variant="h3">
                            {doneTasks}
                        </Typography>

                    </CardContent>

                </Card>

                <Card>

                    <CardContent>

                        <Typography variant="h6">
                            High Priority
                        </Typography>

                        <Typography variant="h3">
                            {highPriorityTasks}
                        </Typography>

                    </CardContent>

                </Card>

                <Card>

                    <CardContent>

                        <Typography variant="h6">
                            Overdue
                        </Typography>

                        <Typography variant="h3">
                            {overdueTasks}
                        </Typography>

                    </CardContent>

                </Card>

            </Box>

        </>

    );

}

export default Dashboard;