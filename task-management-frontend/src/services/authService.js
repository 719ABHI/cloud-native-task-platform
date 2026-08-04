import axios from "axios";

const API_URL =
    `${import.meta.env.VITE_API_URL}/auth`;

export const login = async (email, password) => {

    const response = await axios.post(
        `${API_URL}/login`,
        {
            email,
            password,
        }
    );

    return response.data;
};

export const register = async (userData) => {

    const response = await axios.post(
        `${API_URL}/register`,
        userData
    );

    return response.data;
};