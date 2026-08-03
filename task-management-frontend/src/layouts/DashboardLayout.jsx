import { Box } from "@mui/material";
import { Outlet } from "react-router-dom";

import Header from "../components/Header";
import Sidebar from "../components/Sidebar";

function DashboardLayout() {
    return (
        <>
            <Header />

            <Box
                sx={{
                    display: "flex",
                    minHeight: "calc(100vh - 64px)",
                }}
            >
                <Sidebar />

                <Box
                    sx={{
                        flex: 1,
                        padding: 3,
                    }}
                >
                    <Outlet />
                </Box>
            </Box>
        </>
    );
}

export default DashboardLayout;