/* ==========================================================================
   Apex Bank - Frontend Application Logic & REST API Integration
   ========================================================================== */

const API_BASE_URL = '/api/auth';

// Initialize App on DOM Load
document.addEventListener('DOMContentLoaded', () => {
    checkAuthState();

    // Alert Close Button listener
    document.getElementById('alert-close').addEventListener('click', hideAlert);
});

// 1. Tab Switcher (Login <-> Register)
function switchTab(tab) {
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const tabLogin = document.getElementById('tab-login');
    const tabRegister = document.getElementById('tab-register');

    hideAlert();

    if (tab === 'login') {
        loginForm.classList.remove('hidden');
        registerForm.classList.add('hidden');
        tabLogin.classList.add('active');
        tabRegister.classList.remove('active');
    } else {
        loginForm.classList.add('hidden');
        registerForm.classList.remove('hidden');
        tabLogin.classList.remove('active');
        tabRegister.classList.add('active');
    }
}

// 2. Password Show/Hide Toggle
function togglePasswordVisibility(inputId, iconElement) {
    const input = document.getElementById(inputId);
    if (input.type === 'password') {
        input.type = 'text';
        iconElement.classList.remove('fa-eye');
        iconElement.classList.add('fa-eye-slash');
    } else {
        input.type = 'password';
        iconElement.classList.remove('fa-eye-slash');
        iconElement.classList.add('fa-eye');
    }
}

// 3. Alert Box Utilities
function showAlert(message, type = 'info') {
    const alertBox = document.getElementById('alert-box');
    const alertIcon = document.getElementById('alert-icon');
    const alertMsg = document.getElementById('alert-message');

    alertBox.className = `alert alert-${type}`;
    alertMsg.textContent = message;

    if (type === 'success') {
        alertIcon.className = 'fa-solid fa-circle-check';
    } else if (type === 'danger') {
        alertIcon.className = 'fa-solid fa-triangle-exclamation';
    } else {
        alertIcon.className = 'fa-solid fa-circle-info';
    }

    alertBox.classList.remove('hidden');
}

function hideAlert() {
    const alertBox = document.getElementById('alert-box');
    alertBox.classList.add('hidden');
}

// 4. Handle Registration Submit
async function handleRegister(event) {
    event.preventDefault();
    hideAlert();

    const fullName = document.getElementById('reg-fullname').value.trim();
    const username = document.getElementById('reg-username').value.trim();
    const email = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value;
    const role = document.getElementById('reg-role').value;

    const submitBtn = document.getElementById('btn-register-submit');
    setLoadingState(submitBtn, true);

    try {
        const response = await fetch(`${API_BASE_URL}/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ fullName, username, email, password, role })
        });

        const result = await response.json();

        if (response.ok && result.success) {
            showAlert('Registration successful! Logging you in...', 'success');
            
            // Store token and show dashboard
            if (result.data && result.data.token) {
                localStorage.setItem('apex_jwt_token', result.data.token);
                setTimeout(() => {
                    showDashboard(result.data);
                }, 1000);
            } else {
                setTimeout(() => switchTab('login'), 1500);
            }
        } else {
            let errorMsg = result.message || 'Registration failed';
            if (result.errors) {
                errorMsg = Object.values(result.errors).join(', ');
            }
            showAlert(errorMsg, 'danger');
        }
    } catch (error) {
        console.error('Registration Error:', error);
        showAlert('Network error. Is Spring Boot server running?', 'danger');
    } finally {
        setLoadingState(submitBtn, false);
    }
}

// 5. Handle Login Submit
async function handleLogin(event) {
    event.preventDefault();
    hideAlert();

    const usernameOrEmail = document.getElementById('login-username-email').value.trim();
    const password = document.getElementById('login-password').value;

    const submitBtn = document.getElementById('btn-login-submit');
    setLoadingState(submitBtn, true);

    try {
        const response = await fetch(`${API_BASE_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ usernameOrEmail, password })
        });

        const result = await response.json();

        if (response.ok && result.success && result.data) {
            showAlert('Login successful! Welcome back.', 'success');
            localStorage.setItem('apex_jwt_token', result.data.token);

            setTimeout(() => {
                showDashboard(result.data);
            }, 800);
        } else {
            showAlert(result.message || 'Invalid username or password.', 'danger');
        }
    } catch (error) {
        console.error('Login Error:', error);
        showAlert('Network error. Unable to connect to server.', 'danger');
    } finally {
        setLoadingState(submitBtn, false);
    }
}

// 6. Check Authentication State on Page Load
async function checkAuthState() {
    const token = localStorage.getItem('apex_jwt_token');
    if (!token) return;

    try {
        const response = await fetch(`${API_BASE_URL}/me`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (response.ok) {
            const result = await response.json();
            if (result.success && result.data) {
                showDashboard({
                    token: token,
                    username: result.data.username,
                    email: result.data.email,
                    fullName: result.data.fullName,
                    role: result.data.role
                });
            }
        } else {
            // Token expired or invalid
            localStorage.removeItem('apex_jwt_token');
        }
    } catch (error) {
        console.warn('Could not verify token session:', error);
    }
}

// 7. Show Logged-in Dashboard
function showDashboard(userData) {
    document.getElementById('auth-container').classList.add('hidden');
    document.getElementById('dashboard-container').classList.remove('hidden');
    hideAlert();

    document.getElementById('dash-fullname').textContent = userData.fullName || userData.username;
    document.getElementById('dash-username').textContent = userData.username || '-';
    document.getElementById('dash-email').textContent = userData.email || '-';
    document.getElementById('dash-role').textContent = userData.role || 'ROLE_USER';
    document.getElementById('dash-token').textContent = `Bearer ${userData.token}`;
}

// 8. Handle Logout
function handleLogout() {
    localStorage.removeItem('apex_jwt_token');
    document.getElementById('dashboard-container').classList.add('hidden');
    document.getElementById('auth-container').classList.remove('hidden');

    // Reset forms
    document.getElementById('login-form').reset();
    document.getElementById('register-form').reset();
    switchTab('login');
    showAlert('You have been logged out successfully.', 'info');
}

// 9. Copy Bearer Token Utility
function copyTokenToClipboard() {
    const tokenText = document.getElementById('dash-token').textContent;
    navigator.clipboard.writeText(tokenText).then(() => {
        showAlert('JWT Bearer Token copied to clipboard!', 'success');
    });
}

// Helper: Loading Button State
function setLoadingState(button, isLoading) {
    const textSpan = button.querySelector('.btn-text');
    const spinner = button.querySelector('.spinner');

    if (isLoading) {
        button.disabled = true;
        textSpan.classList.add('hidden');
        spinner.classList.remove('hidden');
    } else {
        button.disabled = false;
        textSpan.classList.remove('hidden');
        spinner.classList.add('hidden');
    }
}
