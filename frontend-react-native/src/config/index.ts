import { Platform } from 'react-native';
import { resolveApiBaseUrl } from './apiUrl';

export const API_BASE_URL = resolveApiBaseUrl(process.env.EXPO_PUBLIC_API_URL, Platform.OS);
