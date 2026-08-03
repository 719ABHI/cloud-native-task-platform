import { useState } from "react";
import { useNavigate } from "react-router-dom";

import {
    Alert,
    Box,
    Button,
    Container,
    Paper,
    TextField,
    Typography,
} from "@mui/material";

import { register } from "../services/authService";

function Register() {

    const navigate = useNavigate();

    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleRegister = async () => {

        setLoading(true);
        setError("");

        try {

            await register({
                username,
                email,
                password,
            });

            navigate("/");

        } catch (error) {

            setError("Registration failed");

        } finally {

            setLoading(false);

        }

    };

    return (

        <Container maxWidth="sm">

            <Paper
                elevation={3}
                sx={{
                    padding: 4,
                    marginTop: 10,
                }}
            >

                <Typography
                    variant="h4"
                    align="center"
                    gutterBottom
                >
                    Register
                </Typography>

                <Box
                    sx={{
                        display: "flex",
                        flexDirection: "column",
                        gap: 2,
                    }}
                >

                    {error && (
                        <Alert severity="error">
                            {error}
                        </Alert>
                    )}

                    <TextField
                        label="Username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                    />

                    <TextField
                        label="Email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />

                    <TextField
                        label="Password"
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />

                    <Button
                        variant="contained"
                        onClick={handleRegister}
                        disabled={loading}
                    >
                        {loading ? "Registering..." : "Register"}
                    </Button>

                    <Button
                        variant="text"
                        onClick={() => navigate("/")}
                    >
                        Back to Login
                    </Button>

                </Box>

            </Paper>

        </Container>

    );

}

export default Register;