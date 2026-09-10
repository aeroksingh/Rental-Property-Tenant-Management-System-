import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import AppLayout from './components/AppLayout';

import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import OwnerProperties from './pages/OwnerProperties';
import OwnerOverdueRent from './pages/OwnerOverdueRent';
import OwnerMaintenance from './pages/OwnerMaintenance';
import TenantHome from './pages/TenantHome';
import TenantRentHistory from './pages/TenantRentHistory';
import TenantMaintenance from './pages/TenantMaintenance';
import NotFound from './pages/NotFound';

function HomeRedirect() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return <Navigate to={user.role === 'OWNER' ? '/owner/properties' : '/tenant/home'} replace />;
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route
            element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }
          >
            <Route
              path="/owner/properties"
              element={
                <ProtectedRoute allowedRole="OWNER">
                  <OwnerProperties />
                </ProtectedRoute>
              }
            />
            <Route
              path="/owner/overdue-rent"
              element={
                <ProtectedRoute allowedRole="OWNER">
                  <OwnerOverdueRent />
                </ProtectedRoute>
              }
            />
            <Route
              path="/owner/maintenance"
              element={
                <ProtectedRoute allowedRole="OWNER">
                  <OwnerMaintenance />
                </ProtectedRoute>
              }
            />

            <Route
              path="/tenant/home"
              element={
                <ProtectedRoute allowedRole="TENANT">
                  <TenantHome />
                </ProtectedRoute>
              }
            />
            <Route
              path="/tenant/rent-history"
              element={
                <ProtectedRoute allowedRole="TENANT">
                  <TenantRentHistory />
                </ProtectedRoute>
              }
            />
            <Route
              path="/tenant/maintenance"
              element={
                <ProtectedRoute allowedRole="TENANT">
                  <TenantMaintenance />
                </ProtectedRoute>
              }
            />
          </Route>

          <Route path="/" element={<HomeRedirect />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
