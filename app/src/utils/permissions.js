import { storeToRefs } from 'pinia';
import { useAuthStore } from '@/stores/auth';

export function can(permission) {
  if (!permission) return true;
  const auth = useAuthStore();
  const { permissions } = storeToRefs(auth);
  return permissions.value?.includes(permission) ?? false;
}
