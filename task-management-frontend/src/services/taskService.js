import axios from "axios";
import { getToken } from "../utils/token";

const API_URL =
    `${import.meta.env.VITE_API_URL}/tasks`;

export const getAllTasks = async () => {

    const response = await axios.get(API_URL, {
        headers: {
            Authorization: `Bearer ${getToken()}`
        }
    });

    return response.data;
};

export const createTask = async (task) => {

    const response = await axios.post(
        API_URL,
        task,
        {
            headers: {
                Authorization: `Bearer ${getToken()}`
            }
        }
    );

    return response.data;
};

export const updateTask = async (id, task) => {

    const response = await axios.put(
        `${API_URL}/${id}`,
        task,
        {
            headers: {
                Authorization: `Bearer ${getToken()}`
            }
        }
    );

    return response.data;
};

export const deleteTask = async (id) => {

    await axios.delete(
        `${API_URL}/${id}`,
        {
            headers: {
                Authorization: `Bearer ${getToken()}`
            }
        }
    );
};