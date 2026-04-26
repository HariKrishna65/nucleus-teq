// Interview Tracker Frontend JavaScript
const API_BASE = 'http://localhost:8080/api';

// Store data globally
let candidates = [];
let interviews = [];

// Initialize the app
document.addEventListener('DOMContentLoaded', function() {
    // Check if we're on dashboard page
    if (document.getElementById('candidatesTableBody')) {
        loadCandidates();
        loadInterviews();
        setupNavigation();
    }
});

// Navigation setup
function setupNavigation() {
    const navLinks = document.querySelectorAll('.nav-links a[data-view]');
    navLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const view = this.getAttribute('data-view');
            switchView(view);
            
            // Update active state
            navLinks.forEach(l => l.classList.remove('active'));
            this.classList.add('active');
        });
    });

    // Logout handler
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            sessionStorage.removeItem('userEmail');
            window.location.href = 'login.html';
        });
    }
}

// Switch between views
function switchView(view) {
    const candidatesView = document.getElementById('candidatesView');
    const interviewsView = document.getElementById('interviewsView');
    
    if (view === 'candidates') {
        candidatesView.style.display = 'block';
        interviewsView.style.display = 'none';
    } else if (view === 'interviews') {
        candidatesView.style.display = 'none';
        interviewsView.style.display = 'block';
    }
}

// ==================== Candidates ====================

// Load all candidates
async function loadCandidates() {
    try {
        const response = await fetch(`${API_BASE}/candidates`);
        candidates = await response.json();
        renderCandidates();
        updateStats();
    } catch (error) {
        console.error('Error loading candidates:', error);
        document.getElementById('candidatesTableBody').innerHTML = 
            '<tr><td colspan="7">Error loading candidates. Make sure backend is running.</td></tr>';
    }
}

// Render candidates table
function renderCandidates() {
    const tbody = document.getElementById('candidatesTableBody');
    if (!tbody) return;

    if (candidates.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7">No candidates found. Add one to get started!</td></tr>';
        return;
    }

    tbody.innerHTML = candidates.map(c => `
        <tr>
            <td>${c.id}</td>
            <td>${c.name}</td>
            <td>${c.email}</td>
            <td>${c.phone || '-'}</td>
            <td><span class="status-badge status-${c.status}">${c.status}</span></td>
            <td>${c.appliedDate ? new Date(c.appliedDate).toLocaleDateString() : '-'}</td>
            <td>
                <button class="btn-small" onclick="editCandidate(${c.id})">Edit</button>
                <button class="btn-small btn-danger" onclick="deleteCandidate(${c.id})">Delete</button>
            </td>
        </tr>
    `).join('');
}

// Add/Edit Candidate
function showAddCandidateModal(candidate = null) {
    const modal = document.getElementById('candidateModal');
    const form = document.getElementById('candidateForm');
    const title = document.getElementById('candidateModalTitle');
    
    if (candidate) {
        title.textContent = 'Edit Candidate';
        document.getElementById('candidateId').value = candidate.id;
        document.getElementById('candidateName').value = candidate.name;
        document.getElementById('candidateEmail').value = candidate.email;
        document.getElementById('candidatePhone').value = candidate.phone || '';
        document.getElementById('candidateStatus').value = candidate.status;
    } else {
        title.textContent = 'Add Candidate';
        form.reset();
        document.getElementById('candidateId').value = '';
    }
    
    modal.style.display = 'block';
}

// Edit candidate
function editCandidate(id) {
    const candidate = candidates.find(c => c.id === id);
    if (candidate) {
        showAddCandidateModal(candidate);
    }
}

// Save candidate
document.getElementById('candidateForm')?.addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const id = document.getElementById('candidateId').value;
    const candidate = {
        name: document.getElementById('candidateName').value,
        email: document.getElementById('candidateEmail').value,
        phone: document.getElementById('candidatePhone').value,
        status: document.getElementById('candidateStatus').value
    };

    try {
        let response;
        if (id) {
            response = await fetch(`${API_BASE}/candidates/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(candidate)
            });
        } else {
            response = await fetch(`${API_BASE}/candidates`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(candidate)
            });
        }

        if (response.ok) {
            closeModal('candidateModal');
            loadCandidates();
        } else {
            alert('Error saving candidate');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error saving candidate. Make sure backend is running.');
    }
});

// Delete candidate
async function deleteCandidate(id) {
    if (!confirm('Are you sure you want to delete this candidate?')) return;
    
    try {
        const response = await fetch(`${API_BASE}/candidates/${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            loadCandidates();
        } else {
            alert('Error deleting candidate');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error deleting candidate. Make sure backend is running.');
    }
}

// ==================== Interviews ====================

// Load all interviews
async function loadInterviews() {
    try {
        const response = await fetch(`${API_BASE}/interviews`);
        interviews = await response.json();
        renderInterviews();
        populateCandidateDropdown();
        updateStats();
    } catch (error) {
        console.error('Error loading interviews:', error);
        document.getElementById('interviewsTableBody').innerHTML = 
            '<tr><td colspan="7">Error loading interviews. Make sure backend is running.</td></tr>';
    }
}

// Render interviews table
function renderInterviews() {
    const tbody = document.getElementById('interviewsTableBody');
    if (!tbody) return;

    if (interviews.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7">No interviews found. Schedule one to get started!</td></tr>';
        return;
    }

    tbody.innerHTML = interviews.map(i => `
        <tr>
            <td>${i.id}</td>
            <td>${i.candidate ? i.candidate.name : 'N/A'}</td>
            <td>${i.interviewerName}</td>
            <td><span class="status-badge status-${i.round}">${i.round.replace('_', ' ')}</span></td>
            <td>${i.scheduledTime ? new Date(i.scheduledTime).toLocaleString() : '-'}</td>
            <td><span class="status-badge status-${i.status}">${i.status}</span></td>
            <td>
                <button class="btn-small" onclick="editInterview(${i.id})">Edit</button>
                <button class="btn-small btn-danger" onclick="deleteInterview(${i.id})">Delete</button>
            </td>
        </tr>
    `).join('');
}

// Populate candidate dropdown
function populateCandidateDropdown() {
    const select = document.getElementById('interviewCandidate');
    if (!select) return;

    select.innerHTML = '<option value="">Select Candidate</option>' +
        candidates.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
}

// Add/Edit Interview
function showAddInterviewModal(interview = null) {
    const modal = document.getElementById('interviewModal');
    const form = document.getElementById('interviewForm');
    const title = document.getElementById('interviewModalTitle');
    
    populateCandidateDropdown();
    
    if (interview) {
        title.textContent = 'Edit Interview';
        document.getElementById('interviewId').value = interview.id;
        document.getElementById('interviewCandidate').value = interview.candidate ? interview.candidate.id : '';
        document.getElementById('interviewerName').value = interview.interviewerName;
        document.getElementById('interviewerEmail').value = interview.interviewerEmail || '';
        document.getElementById('scheduledTime').value = interview.scheduledTime ? interview.scheduledTime.slice(0, 16) : '';
        document.getElementById('interviewRound').value = interview.round;
        document.getElementById('meetingLink').value = interview.meetingLink || '';
        document.getElementById('interviewNotes').value = interview.notes || '';
    } else {
        title.textContent = 'Schedule Interview';
        form.reset();
        document.getElementById('interviewId').value = '';
    }
    
    modal.style.display = 'block';
}

// Edit interview
function editInterview(id) {
    const interview = interviews.find(i => i.id === id);
    if (interview) {
        showAddInterviewModal(interview);
    }
}

// Save interview
document.getElementById('interviewForm')?.addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const id = document.getElementById('interviewId').value;
    const candidateId = document.getElementById('interviewCandidate').value;
    
    const interview = {
        candidate: { id: parseInt(candidateId) },
        interviewerName: document.getElementById('interviewerName').value,
        interviewerEmail: document.getElementById('interviewerEmail').value,
        scheduledTime: document.getElementById('scheduledTime').value,
        round: document.getElementById('interviewRound').value,
        meetingLink: document.getElementById('meetingLink').value,
        notes: document.getElementById('interviewNotes').value,
        status: 'SCHEDULED',
        durationMinutes: 60
    };

    try {
        let response;
        if (id) {
            response = await fetch(`${API_BASE}/interviews/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(interview)
            });
        } else {
            response = await fetch(`${API_BASE}/interviews`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(interview)
            });
        }

        if (response.ok) {
            closeModal('interviewModal');
            loadInterviews();
        } else {
            alert('Error saving interview');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error saving interview. Make sure backend is running.');
    }
});

// Delete interview
async function deleteInterview(id) {
    if (!confirm('Are you sure you want to delete this interview?')) return;
    
    try {
        const response = await fetch(`${API_BASE}/interviews/${id}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            loadInterviews();
        } else {
            alert('Error deleting interview');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error deleting interview. Make sure backend is running.');
    }
}

// ==================== Stats ====================

function updateStats() {
    // Total candidates
    document.getElementById('totalCandidates').textContent = candidates.length;
    
    // Scheduled interviews
    const scheduled = interviews.filter(i => i.status === 'SCHEDULED').length;
    document.getElementById('scheduledInterviews').textContent = scheduled;
    
    // Pending (APPLIED or SCREENING)
    const pending = candidates.filter(c => c.status === 'APPLIED' || c.status === 'SCREENING').length;
    document.getElementById('pendingCandidates').textContent = pending;
    
    // Hired
    const hired = candidates.filter(c => c.status === 'HIRED').length;
    document.getElementById('hiredCandidates').textContent = hired;
}

// ==================== Modal Helpers ====================

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

// Close modal when clicking outside
window.onclick = function(event) {
    if (event.target.classList.contains('modal')) {
        event.target.style.display = 'none';
    }
}