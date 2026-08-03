import { Box, Button } from "@mui/material";
import { useNavigate } from "react-router-dom";

import { logout } from "../utils/token";

function Sidebar() {

    const navigate = useNavigate();

    const handleLogout = () => {

        logout();

        navigate("/");

    };

    return (

        <Box
            sx={{
                width: 220,
                borderRight: "1px solid #ddd",
                padding: 2,
                display: "flex",
                flexDirection: "column",
                gap: 2,
            }}
        >

            <Button
                onClick={() => navigate("/dashboard")}
            >
                Dashboard
            </Button>

            <Button
                onClick={() => navigate("/tasks")}
            >
                Tasks
            </Button>

            <Button
                color="error"
                onClick={handleLogout}
            >
                Logout
            </Button>

        </Box>

    );

}

export default Sidebar;