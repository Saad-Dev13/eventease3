import axios from 'axios';
import { API_BASE_URL } from '../config/api';

// Frontend API service functions (calls backend endpoints)
export const eventService = {
  // Get all events
  getAllEvents: async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/event/`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error.message;
    }
  },

  // Get single event
  getEvent: async (id) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/event/get/${id}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error.message;
    }
  },

  // Create event (requires authentication)
  createEvent: async (eventData, token) => {
    try {
      const response = await axios.post(`${API_BASE_URL}/event/create`, eventData, {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || error.message;
    }
  },

  // Update event (requires authentication)
  updateEvent: async (id, eventData, token) => {
    try {
      const response = await axios.put(`${API_BASE_URL}/event/update/${id}`, eventData, {
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || error.message;
    }
  },

  // Delete event (requires authentication)
  deleteEvent: async (id, token) => {
    try {
      const response = await axios.delete(`${API_BASE_URL}/event/delete/${id}`, {
        headers: {
          Authorization: `Bearer ${token}`
        }
      });
      return response.data;
    } catch (error) {
      throw error.response?.data || error.message;
    }
  }
};
