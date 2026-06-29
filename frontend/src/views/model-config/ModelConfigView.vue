<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1>模型配置</h1>
        <p>维护 AI 中转站、模型名称、调用参数和默认模型。</p>
      </div>
      <a-space>
        <a-button @click="loadConfigs">
          <reload-outlined />
          刷新
        </a-button>
        <a-button type="primary" @click="openCreate">
          <plus-outlined />
          新增配置
        </a-button>
      </a-space>
    </div>

    <a-card :bordered="false" class="filter-card">
      <a-form layout="inline">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" allow-clear placeholder="供应商、模型或 Base URL" @pressEnter="handleSearch" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear :options="statusOptions" class="filter-select" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <search-outlined />
              查询
            </a-button>
            <a-button @click="resetSearch">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card :bordered="false" class="section">
      <a-table
        row-key="id"
        :columns="columns"
        :data-source="configs"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'model'">
            <div class="strong-line">{{ record.modelName }}</div>
            <div class="muted-line">{{ record.provider }} · {{ record.apiStyle }}</div>
          </template>
          <template v-else-if="column.key === 'apiBaseUrl'">
            <span class="mono-text">{{ record.apiBaseUrl }}</span>
          </template>
          <template v-else-if="column.key === 'defaultModel'">
            <a-tag v-if="record.defaultModel" color="blue">默认</a-tag>
            <a-button v-else size="small" @click="setDefault(record.id)">设为默认</a-button>
          </template>
          <template v-else-if="column.key === 'enabled'">
            <a-switch
              :checked="record.enabled"
              checked-children="启用"
              un-checked-children="停用"
              @change="changeStatus(record.id, $event as boolean)"
            />
          </template>
          <template v-else-if="column.key === 'hasApiKey'">
            <a-tag :color="record.hasApiKey ? 'green' : 'orange'">
              {{ record.hasApiKey ? '已配置' : '使用环境变量' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'params'">
            <span>{{ record.maxTokens }} tokens / {{ record.temperature }} / {{ record.timeoutSeconds }}s</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="openEdit(record)">编辑</a-button>
              <a-button size="small" @click="testConnection(record)">测试</a-button>
              <a-popconfirm title="确认删除这个模型配置？" @confirm="removeConfig(record.id)">
                <a-button size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="formOpen"
      :title="editingId ? '编辑模型配置' : '新增模型配置'"
      :confirm-loading="saving"
      width="720px"
      @ok="saveConfig"
    >
      <a-form layout="vertical">
        <a-form-item label="供应商" required>
          <a-input v-model:value="form.provider" placeholder="OpenAI" />
        </a-form-item>
        <a-form-item label="API 风格" required>
          <a-select v-model:value="form.apiStyle" :options="apiStyleOptions" />
        </a-form-item>
        <a-form-item label="API Base URL" required>
          <a-input v-model:value="form.apiBaseUrl" placeholder="https://api.nexustokenai.com" />
        </a-form-item>
        <a-form-item label="模型名称" required>
          <a-input v-model:value="form.modelName" placeholder="gpt-5.5" />
        </a-form-item>
        <a-form-item label="API Key">
          <a-input-password
            v-model:value="form.apiKey"
            :placeholder="editingId ? '留空则保持原 Key 或继续使用环境变量' : '可留空使用 APP_AI_API_KEY'"
          />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="最大输出 Token">
              <a-input-number v-model:value="form.maxTokens" :min="256" :max="32768" class="full-width" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="温度">
              <a-input-number v-model:value="form.temperature" :min="0" :max="2" :step="0.1" class="full-width" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="超时秒数">
              <a-input-number v-model:value="form.timeoutSeconds" :min="5" :max="300" class="full-width" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-space>
          <a-checkbox v-model:checked="form.defaultModel">设为默认模型</a-checkbox>
          <a-checkbox v-model:checked="form.enabled">启用</a-checkbox>
        </a-space>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message, type TablePaginationConfig } from 'ant-design-vue';
import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue';
import {
  createModelConfig,
  deleteModelConfig,
  fetchModelConfigs,
  setDefaultModelConfig,
  testModelConfig,
  updateModelConfig,
  updateModelConfigStatus,
  type ModelConfig,
  type ModelConfigPayload,
} from '@/api/model-config';

const loading = ref(false);
const saving = ref(false);
const formOpen = ref(false);
const editingId = ref<number | null>(null);
const configs = ref<ModelConfig[]>([]);
const total = ref(0);

const query = reactive({
  keyword: '',
  status: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10,
});

const form = reactive({
  provider: 'OpenAI',
  apiStyle: 'RESPONSES',
  apiBaseUrl: 'https://api.nexustokenai.com',
  modelName: 'gpt-5.5',
  apiKey: '',
  maxTokens: 4096,
  temperature: 0.3,
  timeoutSeconds: 60,
  defaultModel: false,
  enabled: true,
});

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
];

const apiStyleOptions = [
  { label: 'Responses API', value: 'RESPONSES' },
  { label: 'OpenAI Compatible', value: 'OPENAI_COMPATIBLE' },
];

const columns = [
  { title: '模型', key: 'model', width: 220 },
  { title: 'Base URL', key: 'apiBaseUrl', dataIndex: 'apiBaseUrl' },
  { title: '参数', key: 'params', width: 220 },
  { title: 'Key', key: 'hasApiKey', width: 140 },
  { title: '默认', key: 'defaultModel', width: 110 },
  { title: '状态', key: 'enabled', width: 120 },
  { title: '操作', key: 'action', width: 210 },
];

const pagination = computed<TablePaginationConfig>(() => ({
  current: query.pageNum,
  pageSize: query.pageSize,
  total: total.value,
  showSizeChanger: true,
  showTotal: (count) => `共 ${count} 条`,
}));

onMounted(loadConfigs);

async function loadConfigs() {
  loading.value = true;
  try {
    const page = await fetchModelConfigs(query);
    configs.value = page.records;
    total.value = page.total;
    query.pageNum = page.pageNum;
    query.pageSize = page.pageSize;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载模型配置失败');
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.pageNum = 1;
  loadConfigs();
}

function resetSearch() {
  query.keyword = '';
  query.status = undefined;
  query.pageNum = 1;
  loadConfigs();
}

function handleTableChange(page: TablePaginationConfig) {
  query.pageNum = page.current || 1;
  query.pageSize = page.pageSize || 10;
  loadConfigs();
}

function openCreate() {
  editingId.value = null;
  Object.assign(form, {
    provider: 'OpenAI',
    apiStyle: 'RESPONSES',
    apiBaseUrl: 'https://api.nexustokenai.com',
    modelName: 'gpt-5.5',
    apiKey: '',
    maxTokens: 4096,
    temperature: 0.3,
    timeoutSeconds: 60,
    defaultModel: false,
    enabled: true,
  });
  formOpen.value = true;
}

function openEdit(record: ModelConfig) {
  editingId.value = record.id;
  Object.assign(form, {
    provider: record.provider,
    apiStyle: record.apiStyle,
    apiBaseUrl: record.apiBaseUrl,
    modelName: record.modelName,
    apiKey: '',
    maxTokens: record.maxTokens,
    temperature: record.temperature,
    timeoutSeconds: record.timeoutSeconds,
    defaultModel: record.defaultModel,
    enabled: record.enabled,
  });
  formOpen.value = true;
}

async function saveConfig() {
  if (!form.provider || !form.apiBaseUrl || !form.modelName) {
    message.warning('请填写供应商、Base URL 和模型名称');
    return;
  }
  saving.value = true;
  try {
    const payload: ModelConfigPayload = {
      provider: form.provider,
      apiStyle: form.apiStyle,
      modelName: form.modelName,
      apiBaseUrl: form.apiBaseUrl,
      apiKey: form.apiKey || undefined,
      maxTokens: form.maxTokens,
      temperature: form.temperature,
      timeoutSeconds: form.timeoutSeconds,
      defaultModel: form.defaultModel,
      status: form.enabled ? 1 : 0,
    };
    if (editingId.value) {
      await updateModelConfig(editingId.value, payload);
      message.success('模型配置已保存');
    } else {
      await createModelConfig(payload);
      message.success('模型配置已创建');
    }
    formOpen.value = false;
    await loadConfigs();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '保存模型配置失败');
  } finally {
    saving.value = false;
  }
}

async function changeStatus(id: number, enabled: boolean) {
  try {
    await updateModelConfigStatus(id, enabled ? 1 : 0);
    message.success('状态已更新');
    await loadConfigs();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '状态更新失败');
  }
}

async function setDefault(id: number) {
  try {
    await setDefaultModelConfig(id);
    message.success('默认模型已更新');
    await loadConfigs();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '设置默认模型失败');
  }
}

async function testConnection(record: ModelConfig) {
  try {
    const result = await testModelConfig(record.id);
    message.success(`${result.message}，耗时 ${result.latencyMs}ms`);
  } catch (error) {
    message.error(error instanceof Error ? error.message : '连接测试失败');
  }
}

async function removeConfig(id: number) {
  try {
    await deleteModelConfig(id);
    message.success('模型配置已删除');
    await loadConfigs();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除模型配置失败');
  }
}
</script>
