import { defineStore } from 'pinia';

let seed = 0;

export const useNotificationStore = defineStore('notifications', {
  state: () => ({
    items: [],
  }),
  actions: {
    push({ type = 'info', title = '', message = '', timeout = 4000 }) {
      const id = ++seed;
      const createdAt = Date.now();
      this.items.push({ id, type, title, message, createdAt });
      if (timeout > 0) {
        setTimeout(() => this.remove(id), timeout);
      }
      return id;
    },
    remove(id) {
      this.items = this.items.filter((item) => item.id !== id);
    },
    clear() {
      this.items = [];
    },
  },
});