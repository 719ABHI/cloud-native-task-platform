export const saveToken = (token) => {
    localStorage.setItem("accessToken", token);
};

export const getToken = () => {
    return localStorage.getItem("accessToken");
};

export const removeToken = () => {
    localStorage.removeItem("accessToken");
};

// -----------------------------
// User Information
// -----------------------------

export const saveUser = (user) => {
    localStorage.setItem("user", JSON.stringify(user));
};

export const getUser = () => {

    const user = localStorage.getItem("user");

    return user ? JSON.parse(user) : null;
};

export const removeUser = () => {
    localStorage.removeItem("user");
};

// -----------------------------
// Logout
// -----------------------------

export const logout = () => {

    removeToken();
    removeUser();

};