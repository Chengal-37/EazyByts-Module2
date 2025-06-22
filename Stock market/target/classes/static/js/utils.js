// js/utils.js

// API endpoints base URL
export const API_BASE_URL = 'http://localhost:8080/api';

/**
 * Displays a custom alert message at the top right of the screen.
 * This function aligns with the custom alert styling in styles.css.
 * @param {string} message The message to display.
 * @param {'success'|'danger'|'warning'|'info'} type The type of alert (for styling).
 */
export function showAlert(message, type) {
    let alertContainer = document.getElementById('alert-container');
    if (!alertContainer) {
        alertContainer = document.createElement('div');
        alertContainer.id = 'alert-container';
        document.body.appendChild(alertContainer);
    }

    const alertDiv = document.createElement('div');
    alertDiv.className = `custom-alert alert-${type}`; // Uses classes defined in styles.css
    alertDiv.innerHTML = `
        <span>${message}</span>
        <button type="button" class="alert-close-btn" aria-label="Close">
            <svg width="1em" height="1em" viewBox="0 0 16 16" class="bi bi-x-lg" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                <path fill-rule="evenodd" d="M13.854 2.146a.5.5 0 0 1 0 .708L8.707 8l5.147 5.146a.5.5 0 0 1-.708.708L8 8.707l-5.146 5.147a.5.5 0 0 1-.708-.708L7.293 8 2.146 2.854a.5.5 0 1 1 .708-.708L8 7.293l5.146-5.147a.5.5 0 0 1 .708 0z"/>
            </svg>
        </button>
    `;

    alertContainer.appendChild(alertDiv);
    // Trigger fade-in and slide-down
    setTimeout(() => {
        alertDiv.style.opacity = '1';
        alertDiv.style.transform = 'translateY(0)';
    }, 10);

    // Close button listener
    alertDiv.querySelector('.alert-close-btn').addEventListener('click', () => {
        alertDiv.style.opacity = '0';
        alertDiv.style.transform = 'translateY(-20px)'; // Slide up before removing
        alertDiv.addEventListener('transitionend', () => alertDiv.remove(), { once: true });
    });

    // Auto-remove after 5 seconds
    setTimeout(() => {
        if (alertDiv) { // Ensure alertDiv still exists before trying to remove
            alertDiv.style.opacity = '0';
            alertDiv.style.transform = 'translateY(-20px)';
            alertDiv.addEventListener('transitionend', () => alertDiv.remove(), { once: true });
        }
    }, 5000);
}