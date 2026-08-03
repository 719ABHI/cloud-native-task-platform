import { AppBar, Toolbar, Typography } from "@mui/material";

function Header() {

    return (
        <AppBar position="static">

            <Toolbar>

                <Typography variant="h6">

                    Cloud Native Task Platform

                </Typography>

            </Toolbar>

        </AppBar>
    );

}

export default Header;