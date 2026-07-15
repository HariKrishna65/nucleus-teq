// Mounts the React app with routing, auth context, toast notifications, and styles.
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import App from './App';
import { AuthProvider } from './AuthContext';
import './styles.css';

ReactDOM.createRoot(document.getElementById('root')).render(
  <BrowserRouter><AuthProvider><App /><ToastContainer /></AuthProvider></BrowserRouter>,
);
