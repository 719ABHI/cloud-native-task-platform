import { BrowserRouter, Routes, Route } from "react-router-dom";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import Tasks from "./pages/Tasks";
import Profile from "./pages/Profile";

import DashboardLayout from "./layouts/DashboardLayout";
import ProtectedRoute from "./components/ProtectedRoute";

function App() {
    return (
        <BrowserRouter>

            <Routes>

                {/* Public Routes */}

                <Route path="/" element={<Login />} />

                <Route path="/register" element={<Register />} />



                {/* Protected Routes */}

                <Route
                    element={
                        <ProtectedRoute>
                            <DashboardLayout />
                        </ProtectedRoute>
                    }
                >

                    <Route
                        path="/dashboard"
                        element={<Dashboard />}
                    />

                    <Route
                        path="/tasks"
                        element={<Tasks />}
                    />

                    <Route
                        path="/profile"
                        element={<Profile />}
                    />

                </Route>

            </Routes>

        </BrowserRouter>
    );
}

export default App;