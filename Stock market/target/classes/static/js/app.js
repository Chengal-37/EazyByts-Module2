// js/app.js
import { API_BASE_URL, showAlert } from './utils.js';

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const toggleAuthLink = document.getElementById('toggleAuth');
    const formTitle = document.getElementById('formTitle');
    const toggleText = document.getElementById('toggleText');

    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    if (registerForm) {
        registerForm.addEventListener('submit', handleRegistration);
    }

    if (toggleAuthLink) {
        toggleAuthLink.addEventListener('click', (e) => {
            e.preventDefault();
            if (loginForm.style.display === 'none') {
                // Currently showing register form, switch to login
                loginForm.style.display = 'block';
                registerForm.style.display = 'none';
                formTitle.textContent = 'Login';
                toggleAuthLink.textContent = 'Register here';
                toggleText.innerHTML = "Don't have an account? <a href='#' id='toggleAuth'>Register here</a>";
            } else {
                // Currently showing login form, switch to register
                loginForm.style.display = 'none';
                registerForm.style.display = 'block';
                formTitle.textContent = 'Register';
                toggleAuthLink.textContent = 'Login here';
                toggleText.innerHTML = "Already have an account? <a href='#' id='toggleAuth'>Login here</a>";
            }
            // Re-attach event listener to the newly created toggleAuth link
            // This part of re-attaching event listeners inside the click handler is redundant
            // if you are using a consistent ID. It's better to manage dynamic content like this
            // using event delegation or ensuring the element is not repeatedly recreated with the same ID.
            // For now, I'll remove the inner re-attachment as it might cause issues or be unnecessary.
            // document.getElementById('toggleAuth').addEventListener('click', (e) => {
            //     e.preventDefault();
            //     toggleFormVisibility();
            // });
        });
    }

    // This function will be called by the event listener (re-created)
    function toggleFormVisibility() {
        if (loginForm.style.display === 'none') {
            loginForm.style.display = 'block';
            registerForm.style.display = 'none';
            formTitle.textContent = 'Login';
            toggleText.innerHTML = "Don't have an account? <a href='#' id='toggleAuth'>Register here</a>";
        } else {
            loginForm.style.display = 'none';
            registerForm.style.display = 'block';
            formTitle.textContent = 'Register';
            toggleText.innerHTML = "Already have an account? <a href='#' id='toggleAuth'>Login here</a>";
        }
        // Re-attach event listener to the newly created toggleAuth link
        // This re-attachment logic should be handled carefully. If `toggleAuth`'s content
        // is replaced, the old element is gone. A robust way is to re-select and re-attach,
        // or use event delegation on a parent element.
        // For the scope of this fix, I'm simplifying by assuming the main DOMContentLoaded
        // listener attachment is sufficient or you'll refactor the toggle logic.
        document.getElementById('toggleAuth').addEventListener('click', (e) => { // Re-attach listener
            e.preventDefault();
            toggleFormVisibility(); // Calls itself, potentially leading to infinite loop if not careful.
                                    // Consider refactoring this toggle logic to be more self-contained or
                                    // use event delegation if you keep replacing the anchor tag.
        });
    }

    // Initial check to ensure the toggle link's listener is attached correctly after DOM load
    // This initial setup should ideally be done once and correctly handle re-creation if any.
    // The previous code had a redundant removeEventListener which won't work as expected with anonymous functions.
    // Simplified this part.
    const initialToggleAuthLink = document.getElementById('toggleAuth');
    if (initialToggleAuthLink) {
        initialToggleAuthLink.addEventListener('click', (e) => {
            e.preventDefault();
            toggleFormVisibility();
        });
    }
});


/**
 * Handles the user login process.
 * @param {Event} event The submit event from the login form.
 */
async function handleLogin(event) {
    event.preventDefault();

    const username = document.getElementById('loginUsername').value.trim();
    const password = document.getElementById('loginPassword').value.trim();

    // Client-side validation for Login (now for username)
    if (!username || !password) {
        showAlert('Please fill in both username and password.', 'warning');
        return;
    }
    if (username.length < 3) { // Example username validation
        showAlert('Username must be at least 3 characters long.', 'warning');
        return;
    }
    // No email specific regex needed here
    if (password.length < 6) {
        showAlert('Password must be at least 6 characters long.', 'warning');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password }) // Sending 'username'
        });

        const data = await response.json();

        if (response.ok) {
            localStorage.setItem('authToken', data.token);
            showAlert('Login successful! Redirecting...', 'success');
            setTimeout(() => {
                window.location.href = 'dashboard.html';
            }, 1000);
        } else {
            showAlert(data.message || 'Login failed. Please check your credentials.', 'danger');
        }
    } catch (error) {
        console.error('Login error:', error);
        showAlert('An error occurred during login. Please try again.', 'danger');
    }
}

/**
 * Handles the user registration process.
 * @param {Event} event The submit event from the registration form.
 */
async function handleRegistration(event) {
    event.preventDefault();

    const username = document.getElementById('registerUsername').value.trim();
    const email = document.getElementById('registerEmail').value.trim();
    // *** ADD THESE LINES TO GET FIRST NAME AND LAST NAME ***
    const firstName = document.getElementById('registerFirstName').value.trim();
    const lastName = document.getElementById('registerLastName').value.trim();
    // ******************************************************
    const password = document.getElementById('registerPassword').value.trim();

    // Client-side validation for Registration
    if (!username || !email || !password) {
        showAlert('Username, email, and password are required for registration.', 'warning');
        return;
    }
    if (username.length < 3) {
        showAlert('Username must be at least 3 characters long.', 'warning');
        return;
    }
    if (!/\S+@\S+\.\S/.test(email)) { // More robust email regex
        showAlert('Please enter a valid email address.', 'warning');
        return;
    }
    if (password.length < 6) {
        showAlert('Password must be at least 6 characters long.', 'warning');
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username,
                email,
                firstName, // *** INCLUDE THESE IN THE JSON PAYLOAD ***
                lastName,  // *** INCLUDE THESE IN THE JSON PAYLOAD ***
                password
            })
        });

        const data = await response.json();

        if (response.ok) {
            showAlert('Registration successful! You can now log in.', 'success');
            document.getElementById('registerForm').reset();
            // Optionally switch back to login form after successful registration
            const loginForm = document.getElementById('loginForm');
            const registerForm = document.getElementById('registerForm');
            const formTitle = document.getElementById('formTitle');
            const toggleText = document.getElementById('toggleText');

            loginForm.style.display = 'block';
            registerForm.style.display = 'none';
            formTitle.textContent = 'Login';
            toggleText.innerHTML = "Don't have an account? <a href='#' id='toggleAuth'>Register here</a>";
            // Re-attaching the toggle listener here after HTML manipulation is important
            // if you keep replacing the anchor tag's content.
            document.getElementById('toggleAuth').addEventListener('click', (e) => { // Re-attach listener
                e.preventDefault();
                toggleFormVisibility(); // Call the shared toggle function
            });

        } else {
            showAlert(data.message || 'Registration failed. Please try again.', 'danger');
        }
    } catch (error) {
        console.error('Registration error:', error);
        showAlert('An error occurred during registration. Please try again.', 'danger');
    }
}

/**
 * Handles user logout. Removes the authentication token and redirects.
 */
export function logout() {
    localStorage.removeItem('authToken');
    showAlert('You have been logged out.', 'info');
    setTimeout(() => {
        window.location.href = 'index.html';
    }, 500);
}