<template>
  <div class="space-y-8">
    <header class="flex flex-col gap-4 rounded-2xl bg-white p-6 shadow-sm lg:flex-row lg:items-center lg:justify-between">
      <div>
        <h1 class="text-2xl font-semibold text-slate-900">{{ t('accessControl.title') }}</h1>
        <p class="text-sm text-slate-600">{{ t('accessControl.subtitle') }}</p>
      </div>
      <RouterLink
        :to="{ name: 'user-roles' }"
        class="btn-secondary"
      >
        {{ t('accessControl.links.manageUserRoles') }}
      </RouterLink>
    </header>

    <div class="grid gap-6 lg:grid-cols-2">
      <section class="card">
        <header class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 class="text-lg font-semibold text-slate-900">{{ t('accessControl.sections.actions.title') }}</h2>
            <p class="text-sm text-slate-600">{{ t('accessControl.sections.actions.description') }}</p>
          </div>
          <div class="flex gap-2">
            <button type="button" class="btn-secondary" @click="loadActions" :disabled="actionsLoading">
              <ArrowPathIcon class="h-4 w-4" />
              <span>{{ t('common.actions.refresh') }}</span>
            </button>
            <button type="button" class="btn-primary" @click="openActionModal()">
              <PlusIcon class="h-4 w-4" />
              <span>{{ t('common.actions.create') }}</span>
            </button>
          </div>
        </header>

        <div class="mt-4">
          <div v-if="actionsLoading" class="flex items-center gap-2 rounded-xl bg-slate-50 p-4 text-sm text-slate-500">
            <span class="h-4 w-4 animate-spin rounded-full border-2 border-primary-100 border-t-primary-600"></span>
            {{ t('common.actions.loading') }}
          </div>
          <p v-else-if="actionsError" class="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-600">{{ actionsError }}</p>
          <p v-else-if="!actions.length" class="rounded-xl bg-slate-50 px-4 py-3 text-sm text-slate-500">{{ t('accessControl.sections.actions.empty') }}</p>
          <div v-else class="overflow-hidden rounded-xl border border-slate-200">
            <table class="min-w-full divide-y divide-slate-200">
              <thead class="bg-slate-50">
                <tr>
                  <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('accessControl.sections.actions.columns.name') }}</th>
                  <th class="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('accessControl.sections.actions.columns.actions') }}</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-200 bg-white">
                <tr v-for="action in actions" :key="action.id">
                  <td class="px-4 py-3 text-sm font-medium text-slate-700">{{ action.name }}</td>
                  <td class="px-4 py-3 text-right">
                    <div class="flex justify-end gap-2">
                      <button type="button" class="rounded-lg border border-slate-200 p-2 text-slate-500 hover:border-primary-200 hover:text-primary-600" @click="openActionModal(action)">
                        <PencilSquareIcon class="h-4 w-4" />
                        <span class="sr-only">{{ t('common.actions.edit') }}</span>
                      </button>
                      <button type="button" class="rounded-lg border border-transparent bg-red-50 p-2 text-red-600 hover:border-red-200 hover:bg-red-100" @click="openDelete('actions', action)">
                        <TrashIcon class="h-4 w-4" />
                        <span class="sr-only">{{ t('common.actions.delete') }}</span>
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>

      <section class="card">
        <header class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 class="text-lg font-semibold text-slate-900">{{ t('accessControl.sections.objects.title') }}</h2>
            <p class="text-sm text-slate-600">{{ t('accessControl.sections.objects.description') }}</p>
          </div>
          <div class="flex gap-2">
            <button type="button" class="btn-secondary" @click="loadObjects" :disabled="objectsLoading">
              <ArrowPathIcon class="h-4 w-4" />
              <span>{{ t('common.actions.refresh') }}</span>
            </button>
            <button type="button" class="btn-primary" @click="openObjectModal()">
              <PlusIcon class="h-4 w-4" />
              <span>{{ t('common.actions.create') }}</span>
            </button>
          </div>
        </header>

        <div class="mt-4">
          <div v-if="objectsLoading" class="flex items-center gap-2 rounded-xl bg-slate-50 p-4 text-sm text-slate-500">
            <span class="h-4 w-4 animate-spin rounded-full border-2 border-primary-100 border-t-primary-600"></span>
            {{ t('common.actions.loading') }}
          </div>
          <p v-else-if="objectsError" class="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-600">{{ objectsError }}</p>
          <p v-else-if="!objects.length" class="rounded-xl bg-slate-50 px-4 py-3 text-sm text-slate-500">{{ t('accessControl.sections.objects.empty') }}</p>
          <div v-else class="overflow-hidden rounded-xl border border-slate-200">
            <table class="min-w-full divide-y divide-slate-200">
              <thead class="bg-slate-50">
                <tr>
                  <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('accessControl.sections.objects.columns.name') }}</th>
                  <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('accessControl.sections.objects.columns.description') }}</th>
                  <th class="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('accessControl.sections.objects.columns.actions') }}</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-200 bg-white">
                <tr v-for="object in objects" :key="object.id">
                  <td class="px-4 py-3 text-sm font-medium text-slate-700">{{ object.name }}</td>
                  <td class="px-4 py-3 text-sm text-slate-600">{{ object.description || t('common.placeholders.empty') }}</td>
                  <td class="px-4 py-3 text-right">
                    <div class="flex justify-end gap-2">
                      <button type="button" class="rounded-lg border border-slate-200 p-2 text-slate-500 hover:border-primary-200 hover:text-primary-600" @click="openObjectModal(object)">
                        <PencilSquareIcon class="h-4 w-4" />
                        <span class="sr-only">{{ t('common.actions.edit') }}</span>
                      </button>
                      <button type="button" class="rounded-lg border border-transparent bg-red-50 p-2 text-red-600 hover:border-red-200 hover:bg-red-100" @click="openDelete('objects', object)">
                        <TrashIcon class="h-4 w-4" />
                        <span class="sr-only">{{ t('common.actions.delete') }}</span>
                      </button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </section>
    </div>

    <section class="card">
      <header class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 class="text-lg font-semibold text-slate-900">{{ t('accessControl.sections.permissions.title') }}</h2>
          <p class="text-sm text-slate-600">{{ t('accessControl.sections.permissions.description') }}</p>
        </div>
        <div class="flex gap-2">
          <button type="button" class="btn-secondary" @click="loadPermissions" :disabled="permissionsLoading">
            <ArrowPathIcon class="h-4 w-4" />
            <span>{{ t('common.actions.refresh') }}</span>
          </button>
          <button type="button" class="btn-primary" @click="openPermissionModal()">
            <PlusIcon class="h-4 w-4" />
            <span>{{ t('common.actions.create') }}</span>
          </button>
        </div>
      </header>

      <div class="mt-4">
        <div v-if="permissionsLoading" class="flex items-center gap-2 rounded-xl bg-slate-50 p-4 text-sm text-slate-500">
          <span class="h-4 w-4 animate-spin rounded-full border-2 border-primary-100 border-t-primary-600"></span>
          {{ t('common.actions.loading') }}
        </div>
        <p v-else-if="permissionsError" class="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-600">{{ permissionsError }}</p>
        <p v-else-if="!permissions.length" class="rounded-xl bg-slate-50 px-4 py-3 text-sm text-slate-500">{{ t('accessControl.sections.permissions.empty') }}</p>
        <div v-else class="overflow-x-auto">
          <table class="min-w-full divide-y divide-slate-200">
            <thead class="bg-slate-50">
              <tr>
                <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('accessControl.sections.permissions.columns.code') }}</th>
                <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('accessControl.sections.permissions.columns.object') }}</th>
                <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('accessControl.sections.permissions.columns.action') }}</th>
                <th class="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('accessControl.sections.permissions.columns.description') }}</th>
                <th class="px-4 py-3 text-right text-xs font-semibold uppercase tracking-wide text-slate-500">{{ t('accessControl.sections.permissions.columns.actions') }}</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-200 bg-white">
              <tr v-for="permission in permissions" :key="permission.id">
                <td class="px-4 py-3 text-sm font-semibold text-slate-700">{{ permission.code }}</td>
                <td class="px-4 py-3 text-sm text-slate-600">{{ permission.object?.name }}</td>
                <td class="px-4 py-3 text-sm text-slate-600">{{ permission.action?.name }}</td>
                <td class="px-4 py-3 text-sm text-slate-600">{{ permission.description || t('common.placeholders.empty') }}</td>
                <td class="px-4 py-3 text-right">
                  <div class="flex justify-end gap-2">
                    <button type="button" class="rounded-lg border border-slate-200 p-2 text-slate-500 hover:border-primary-200 hover:text-primary-600" @click="openPermissionModal(permission)">
                      <PencilSquareIcon class="h-4 w-4" />
                      <span class="sr-only">{{ t('common.actions.edit') }}</span>
                    </button>
                    <button type="button" class="rounded-lg border border-transparent bg-red-50 p-2 text-red-600 hover:border-red-200 hover:bg-red-100" @click="openDelete('permissions', permission)">
                      <TrashIcon class="h-4 w-4" />
                      <span class="sr-only">{{ t('common.actions.delete') }}</span>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>

    <section class="card">
      <header class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h2 class="text-lg font-semibold text-slate-900">{{ t('accessControl.sections.roles.title') }}</h2>
          <p class="text-sm text-slate-600">{{ t('accessControl.sections.roles.description') }}</p>
        </div>
        <div class="flex gap-2">
          <button type="button" class="btn-secondary" @click="loadRoles" :disabled="rolesLoading">
            <ArrowPathIcon class="h-4 w-4" />
            <span>{{ t('common.actions.refresh') }}</span>
          </button>
          <button type="button" class="btn-primary" @click="openRoleModal()">
            <PlusIcon class="h-4 w-4" />
            <span>{{ t('common.actions.create') }}</span>
          </button>
        </div>
      </header>

      <div class="mt-4">
        <div v-if="rolesLoading" class="flex items-center gap-2 rounded-xl bg-slate-50 p-4 text-sm text-slate-500">
          <span class="h-4 w-4 animate-spin rounded-full border-2 border-primary-100 border-t-primary-600"></span>
          {{ t('common.actions.loading') }}
        </div>
        <p v-else-if="rolesError" class="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-600">{{ rolesError }}</p>
        <p v-else-if="!roles.length" class="rounded-xl bg-slate-50 px-4 py-3 text-sm text-slate-500">{{ t('accessControl.sections.roles.empty') }}</p>
        <div v-else class="space-y-3">
          <div v-for="role in roles" :key="role.id" class="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
            <div class="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
              <div>
                <h3 class="text-base font-semibold text-slate-900">{{ role.name }}</h3>
                <p class="text-sm text-slate-600">{{ role.description || t('common.placeholders.empty') }}</p>
              </div>
              <div class="flex gap-2">
                <button type="button" class="btn-secondary" @click="openRoleModal(role)">
                  <PencilSquareIcon class="h-4 w-4" />
                  <span>{{ t('common.actions.edit') }}</span>
                </button>
                <button type="button" class="btn-secondary text-red-600 hover:border-red-200 hover:text-red-600" @click="openDelete('roles', role)">
                  <TrashIcon class="h-4 w-4" />
                  <span>{{ t('common.actions.delete') }}</span>
                </button>
              </div>
            </div>
            <div class="mt-3 flex flex-wrap gap-2">
              <span
                v-for="permission in role.permissions"
                :key="permission.id"
                class="rounded-full bg-primary-50 px-3 py-1 text-xs font-semibold text-primary-700"
              >
                {{ permission.code }}
              </span>
              <span v-if="!role.permissions?.length" class="text-sm text-slate-500">{{ t('accessControl.sections.roles.noPermissions') }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <ActionFormModal
      v-model="actionModalOpen"
      :action="activeAction"
      :submitting="actionSaving"
      :error="actionFormError"
      @submit="handleActionSubmit"
      @close="resetActionState"
    />
    <ObjectFormModal
      v-model="objectModalOpen"
      :object="activeObject"
      :submitting="objectSaving"
      :error="objectFormError"
      @submit="handleObjectSubmit"
      @close="resetObjectState"
    />
    <PermissionFormModal
      v-model="permissionModalOpen"
      :permission="activePermission"
      :actions="actions"
      :objects="objects"
      :submitting="permissionSaving"
      :error="permissionFormError"
      @submit="handlePermissionSubmit"
      @close="resetPermissionState"
    />
    <RoleFormModal
      v-model="roleModalOpen"
      :role="activeRole"
      :permissions="permissions"
      :submitting="roleSaving"
      :error="roleFormError"
      @submit="handleRoleSubmit"
      @close="resetRoleState"
    />

    <ConfirmDialog
      v-model="confirmState.open"
      :title="confirmTitle"
      :message="confirmMessage"
      :confirm-label="t('common.actions.delete')"
      @confirm="handleConfirmDelete"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { RouterLink } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useNotificationStore } from '@/stores/notifications';
import { ArrowPathIcon, PencilSquareIcon, PlusIcon, TrashIcon } from '@heroicons/vue/24/outline';
import ConfirmDialog from '@/components/ConfirmDialog.vue';
import ActionFormModal from '@/components/access-control/ActionFormModal.vue';
import ObjectFormModal from '@/components/access-control/ObjectFormModal.vue';
import PermissionFormModal from '@/components/access-control/PermissionFormModal.vue';
import RoleFormModal from '@/components/access-control/RoleFormModal.vue';
import {
  listActions,
  createAction,
  updateAction,
  deleteAction,
  listObjects,
  createObject,
  updateObject,
  deleteObject,
  listPermissions,
  createPermission,
  updatePermission,
  deletePermission,
} from '@/services/accessControl';
import { getRoles, createRole, updateRole, deleteRole } from '@/services/roles';

const { t } = useI18n();
const notifications = useNotificationStore();

const actions = ref([]);
const actionsLoading = ref(false);
const actionsError = ref('');
const actionModalOpen = ref(false);
const activeAction = ref(null);
const actionSaving = ref(false);
const actionFormError = ref('');

const objects = ref([]);
const objectsLoading = ref(false);
const objectsError = ref('');
const objectModalOpen = ref(false);
const activeObject = ref(null);
const objectSaving = ref(false);
const objectFormError = ref('');

const permissions = ref([]);
const permissionsLoading = ref(false);
const permissionsError = ref('');
const permissionModalOpen = ref(false);
const activePermission = ref(null);
const permissionSaving = ref(false);
const permissionFormError = ref('');

const roles = ref([]);
const rolesLoading = ref(false);
const rolesError = ref('');
const roleModalOpen = ref(false);
const activeRole = ref(null);
const roleSaving = ref(false);
const roleFormError = ref('');

const confirmState = reactive({ open: false, entity: '', item: null });

const targetLabel = computed(() => {
  if (!confirmState.item) return '';
  if (confirmState.entity === 'permissions') {
    return confirmState.item.code;
  }
  return confirmState.item.name;
});

const confirmTitle = computed(() => confirmState.entity ? t(`accessControl.sections.${confirmState.entity}.confirmDelete.title`) : '');
const confirmMessage = computed(() => confirmState.entity ? t(`accessControl.sections.${confirmState.entity}.confirmDelete.message`, { name: targetLabel.value }) : '');

const handleError = (error) => error?.response?.data?.details ?? error?.message ?? t('common.error');

const loadActions = async () => {
  actionsLoading.value = true;
  actionsError.value = '';
  try {
    const { data } = await listActions();
    actions.value = data ?? [];
  } catch (error) {
    actionsError.value = handleError(error);
  } finally {
    actionsLoading.value = false;
  }
};

const loadObjects = async () => {
  objectsLoading.value = true;
  objectsError.value = '';
  try {
    const { data } = await listObjects();
    objects.value = data ?? [];
  } catch (error) {
    objectsError.value = handleError(error);
  } finally {
    objectsLoading.value = false;
  }
};

const loadPermissions = async () => {
  permissionsLoading.value = true;
  permissionsError.value = '';
  try {
    const { data } = await listPermissions();
    permissions.value = data ?? [];
  } catch (error) {
    permissionsError.value = handleError(error);
  } finally {
    permissionsLoading.value = false;
  }
};

const loadRoles = async () => {
  rolesLoading.value = true;
  rolesError.value = '';
  try {
    const { data } = await getRoles();
    roles.value = data ?? [];
  } catch (error) {
    rolesError.value = handleError(error);
  } finally {
    rolesLoading.value = false;
  }
};

const openActionModal = (action = null) => {
  activeAction.value = action ? { ...action } : null;
  actionFormError.value = '';
  actionModalOpen.value = true;
};

const resetActionState = () => {
  actionModalOpen.value = false;
  activeAction.value = null;
  actionFormError.value = '';
};

const handleActionSubmit = async (payload) => {
  actionSaving.value = true;
  actionFormError.value = '';
  const name = payload?.name?.trim();
  if (!name) {
    actionFormError.value = t('accessControl.sections.actions.validation.name');
    actionSaving.value = false;
    return;
  }
  try {
    const request = { name: name.toUpperCase() };
    if (activeAction.value?.id) {
      await updateAction(activeAction.value.id, request);
      notifications.push({
        type: 'success',
        title: t('accessControl.notifications.actions.updated.title'),
        message: t('accessControl.notifications.actions.updated.message', { name: request.name }),
      });
    } else {
      await createAction(request);
      notifications.push({
        type: 'success',
        title: t('accessControl.notifications.actions.created.title'),
        message: t('accessControl.notifications.actions.created.message', { name: request.name }),
      });
    }
    await loadActions();
    await loadPermissions();
    resetActionState();
  } catch (error) {
    actionFormError.value = handleError(error);
  } finally {
    actionSaving.value = false;
  }
};

const openObjectModal = (object = null) => {
  activeObject.value = object ? { ...object } : null;
  objectFormError.value = '';
  objectModalOpen.value = true;
};

const resetObjectState = () => {
  objectModalOpen.value = false;
  activeObject.value = null;
  objectFormError.value = '';
};

const handleObjectSubmit = async (payload) => {
  objectSaving.value = true;
  objectFormError.value = '';
  const name = payload?.name?.trim();
  if (!name) {
    objectFormError.value = t('accessControl.sections.objects.validation.name');
    objectSaving.value = false;
    return;
  }
  try {
    const request = {
      name: name.toUpperCase(),
      description: payload?.description?.trim() || null,
    };
    if (activeObject.value?.id) {
      await updateObject(activeObject.value.id, request);
      notifications.push({
        type: 'success',
        title: t('accessControl.notifications.objects.updated.title'),
        message: t('accessControl.notifications.objects.updated.message', { name: request.name }),
      });
    } else {
      await createObject(request);
      notifications.push({
        type: 'success',
        title: t('accessControl.notifications.objects.created.title'),
        message: t('accessControl.notifications.objects.created.message', { name: request.name }),
      });
    }
    await loadObjects();
    await loadPermissions();
    resetObjectState();
  } catch (error) {
    objectFormError.value = handleError(error);
  } finally {
    objectSaving.value = false;
  }
};

const openPermissionModal = async (permission = null) => {
  activePermission.value = permission ? { ...permission } : null;
  permissionFormError.value = '';
  permissionModalOpen.value = true;
};

const resetPermissionState = () => {
  permissionModalOpen.value = false;
  activePermission.value = null;
  permissionFormError.value = '';
};

const handlePermissionSubmit = async (payload) => {
  permissionSaving.value = true;
  permissionFormError.value = '';
  const code = payload?.code?.trim();
  if (!code || !payload?.actionId || !payload?.objectId) {
    permissionFormError.value = t('accessControl.sections.permissions.validation.required');
    permissionSaving.value = false;
    return;
  }
  try {
    const request = {
      code: code.toUpperCase(),
      description: payload?.description?.trim() || null,
      actionId: payload.actionId,
      objectId: payload.objectId,
    };
    if (activePermission.value?.id) {
      await updatePermission(activePermission.value.id, request);
      notifications.push({
        type: 'success',
        title: t('accessControl.notifications.permissions.updated.title'),
        message: t('accessControl.notifications.permissions.updated.message', { code: request.code }),
      });
    } else {
      await createPermission(request);
      notifications.push({
        type: 'success',
        title: t('accessControl.notifications.permissions.created.title'),
        message: t('accessControl.notifications.permissions.created.message', { code: request.code }),
      });
    }
    await loadPermissions();
    await loadRoles();
    resetPermissionState();
  } catch (error) {
    permissionFormError.value = handleError(error);
  } finally {
    permissionSaving.value = false;
  }
};

const openRoleModal = (role = null) => {
  activeRole.value = role ? { ...role } : null;
  roleFormError.value = '';
  roleModalOpen.value = true;
};

const resetRoleState = () => {
  roleModalOpen.value = false;
  activeRole.value = null;
  roleFormError.value = '';
};

const handleRoleSubmit = async (payload) => {
  roleSaving.value = true;
  roleFormError.value = '';
  const name = payload?.name?.trim();
  if (!name) {
    roleFormError.value = t('accessControl.sections.roles.validation.name');
    roleSaving.value = false;
    return;
  }
  try {
    const request = {
      name,
      description: payload?.description?.trim() || null,
      permissions: (payload?.permissionIds ?? []).map((id) => ({ id })),
    };
    if (activeRole.value?.id) {
      await updateRole(activeRole.value.id, request);
      notifications.push({
        type: 'success',
        title: t('accessControl.notifications.roles.updated.title'),
        message: t('accessControl.notifications.roles.updated.message', { name: request.name }),
      });
    } else {
      await createRole(request);
      notifications.push({
        type: 'success',
        title: t('accessControl.notifications.roles.created.title'),
        message: t('accessControl.notifications.roles.created.message', { name: request.name }),
      });
    }
    await loadRoles();
    resetRoleState();
  } catch (error) {
    roleFormError.value = handleError(error);
  } finally {
    roleSaving.value = false;
  }
};

const openDelete = (entity, item) => {
  confirmState.entity = entity;
  confirmState.item = item;
  confirmState.open = true;
};

const clearConfirm = () => {
  confirmState.open = false;
  confirmState.entity = '';
  confirmState.item = null;
};

const handleConfirmDelete = async () => {
  if (!confirmState.entity || !confirmState.item) {
    clearConfirm();
    return;
  }
  try {
    if (confirmState.entity === 'actions') {
      await deleteAction(confirmState.item.id);
      notifications.push({
        type: 'success',
        title: t('accessControl.notifications.actions.deleted.title'),
        message: t('accessControl.notifications.actions.deleted.message', { name: confirmState.item.name }),
      });
      await loadActions();
      await loadPermissions();
    } else if (confirmState.entity === 'objects') {
      await deleteObject(confirmState.item.id);
      notifications.push({
        type: 'success',
        title: t('accessControl.notifications.objects.deleted.title'),
        message: t('accessControl.notifications.objects.deleted.message', { name: confirmState.item.name }),
      });
      await loadObjects();
      await loadPermissions();
    } else if (confirmState.entity === 'permissions') {
      await deletePermission(confirmState.item.id);
      notifications.push({
        type: 'success',
        title: t('accessControl.notifications.permissions.deleted.title'),
        message: t('accessControl.notifications.permissions.deleted.message', { code: confirmState.item.code }),
      });
      await loadPermissions();
      await loadRoles();
    } else if (confirmState.entity === 'roles') {
      await deleteRole(confirmState.item.id);
      notifications.push({
        type: 'success',
        title: t('accessControl.notifications.roles.deleted.title'),
        message: t('accessControl.notifications.roles.deleted.message', { name: confirmState.item.name }),
      });
      await loadRoles();
    }
  } catch (error) {
    notifications.push({
      type: 'error',
      title: t('accessControl.notifications.error.title'),
      message: handleError(error),
    });
  } finally {
    clearConfirm();
  }
};

onMounted(async () => {
  await Promise.all([loadActions(), loadObjects(), loadPermissions(), loadRoles()]);
});
</script>
