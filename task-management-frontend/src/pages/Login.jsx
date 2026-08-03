import { useEffect, useState } from "react";
import { login } from "../services/authService";
import {
    getToken,
    saveToken,
    saveUser,
} from "../utils/token";
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

function Login() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    useEffect(() => {

        if (getToken()) {
            navigate("/dashboard");
        }

    }, [navigate]);

    const handleLogin = async () => {
        setError("");
        setLoading(true);
        try {

            const response = await login(email, password);

            console.log(response);

            saveToken(response.accessToken);

            saveUser(response.user);

            navigate("/dashboard");


        } catch (error) {

            setError("Invalid email or password");
        }
        finally {

            setLoading(false);

        }

    };

    return (
        <Container maxWidth="sm">
            <Paper elevation={3} sx={{ padding: 4, marginTop: 10 }}>
                <Typography variant="h4" align="center" gutterBottom>
                    Cloud Native Task Platform
                </Typography>

                <Typography
                    variant="subtitle1"
                    align="center"
                    sx={{ marginBottom: 3 }}
                >
                    Login to continue
                </Typography>

                <Box
                    sx={{
                        display: "flex",
                        flexDirection: "column",
                        gap: 2,
                    }}
                >
                    {
                        error && (
                            <Alert severity="error">
                                {error}
                            </Alert>
                        )
                    }
                    <TextField
                        label="Email"
                        type="email"
                        fullWidth
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />

                    <TextField
                        label="Password"
                        type="password"
                        fullWidth
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />


                    <Button
                        variant="contained"
                        fullWidth
                        size="large"
                        disabled={loading}
                        onClick={handleLogin}
                    >
                        {loading ? "Logging in..." : "Login"}
                    </Button>

                    <Button
                        variant="text"
                        onClick={() => navigate("/register")}
                    >
                        Create Account
                    </Button>
                </Box>
            </Paper>
        </Container>
    );
}

export default Login;