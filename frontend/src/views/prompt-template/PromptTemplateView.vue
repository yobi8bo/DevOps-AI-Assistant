<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1>Prompt 模板</h1>
        <p>按故障类型维护诊断提示词、版本和默认模板。</p>
      </div>
      <a-space>
        <a-button @click="loadTemplates">
          <reload-outlined />
          刷新
        </a-button>
        <a-button type="primary" @click="openCreate">
          <plus-outlined />
          新增模板
        </a-button>
      </a-space>
    </div>

    <a-card :bordered="false" class="filter-card">
      <a-form layout="inline">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" allow-clear placeholder="模板名称或内容" @pressEnter="handleSearch" />
        </a-form-item>
        <a-form-item label="类型">
          <a-select v-model:value="query.category" allow-clear :options="categoryOptions" class="filter-select" />
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
        :data-source="templates"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <div class="strong-line">{{ record.name }}</div>
            <div class="muted-line">{{ record.category || '通用模板' }}</div>
          </template>
          <template v-else-if="column.key === 'defaultTemplate'">
            <a-tag v-if="record.defaultTemplate" color="blue">默认</a-tag>
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
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="openEdit(record.id)">编辑</a-button>
              <a-popconfirm title="确认删除这个 Prompt 模板？" @confirm="removeTemplate(record.id)">
                <a-button size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="formOpen"
      :title="editingId ? '编辑 Prompt 模板' : '新增 Prompt 模板'"
      :confirm-loading="saving"
      width="920px"
      @ok="saveTemplate"
    >
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="10">
            <a-form-item label="模板名称" required>
              <a-input v-model:value="form.name" placeholder="通用运维排障模板" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="适用类型">
              <a-select v-model:value="form.category" allow-clear :options="categoryOptions" placeholder="通用" />
            </a-form-item>
          </a-col>
          <a-col :span="4">
            <a-form-item label="版本">
              <a-input v-model:value="form.version" placeholder="1.0.0" />
            </a-form-item>
          </a-col>
          <a-col :span="4">
            <a-form-item label="状态">
              <a-switch v-model:checked="form.enabled" checked-children="启用" un-checked-children="停用" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item label="模板内容" required>
          <a-textarea v-model:value="form.content" :rows="18" class="template-editor" />
        </a-form-item>

        <div class="template-footer">
          <a-checkbox v-model:checked="form.defaultTemplate">设为默认模板</a-checkbox>
          <a-space wrap>
            <a-tag v-for="variable in variables" :key="variable" class="variable-tag" @click="insertVariable(variable)">
              {{ variable }}
            </a-tag>
          </a-space>
        </div>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message, type TablePaginationConfig } from 'ant-design-vue';
import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue';
import {
  createPromptTemplate,
  deletePromptTemplate,
  fetchPromptTemplate,
  fetchPromptTemplates,
  setDefaultPromptTemplate,
  updatePromptTemplate,
  updatePromptTemplateStatus,
  type PromptTemplatePayload,
  type PromptTemplateSummary,
} from '@/api/prompt-template';

const loading = ref(false);
const saving = ref(false);
const formOpen = ref(false);
const editingId = ref<number | null>(null);
const templates = ref<PromptTemplateSummary[]>([]);
const total = ref(0);

const query = reactive({
  keyword: '',
  category: undefined as string | undefined,
  status: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10,
});

const defaultContent = `请基于以下运维故障信息进行诊断，并返回结构化 JSON。

分析要求：
1. 结论必须谨慎，不能编造用户没有提供的环境事实。
2. 命令建议优先使用只读检查命令。
3. 涉及删除、格式化、重启、清理、防火墙变更、数据库写操作时必须标记 HIGH 或 CRITICAL，并写明风险提示。
4. 如果信息不足，把需要补充的信息放入 needMoreInfo。
5. riskLevel 只能是 LOW、MEDIUM、HIGH、CRITICAL。

标题：{{title}}
故障类型：{{category}}
运行环境：{{environment}}
操作系统：{{osInfo}}
中间件：{{middleware}}
服务类型：{{serviceType}}
是否生产环境：{{isProduction}}
紧急程度：{{urgencyLevel}}

故障描述：
{{description}}

日志内容：
{{logContent}}

命令输出：
{{commandOutput}}`;

const form = reactive({
  name: '',
  category: undefined as string | undefined,
  content: defaultContent,
  version: '1.0.0',
  defaultTemplate: false,
  enabled: true,
});

const categoryOptions = [
  'Linux',
  'Docker',
  'Kubernetes',
  'Jenkins',
  'Nginx',
  'MySQL',
  'Redis',
  'Spring Boot',
].map((value) => ({ label: value, value }));

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
];

const variables = [
  '{{title}}',
  '{{category}}',
  '{{environment}}',
  '{{osInfo}}',
  '{{middleware}}',
  '{{serviceType}}',
  '{{isProduction}}',
  '{{urgencyLevel}}',
  '{{description}}',
  '{{logContent}}',
  '{{commandOutput}}',
];

const columns = [
  { title: '模板', key: 'name', dataIndex: 'name' },
  { title: '版本', key: 'version', dataIndex: 'version', width: 120 },
  { title: '默认', key: 'defaultTemplate', width: 120 },
  { title: '状态', key: 'enabled', width: 120 },
  { title: '更新时间', key: 'updatedAt', dataIndex: 'updatedAt', width: 190 },
  { title: '操作', key: 'action', width: 150 },
];

const pagination = computed<TablePaginationConfig>(() => ({
  current: query.pageNum,
  pageSize: query.pageSize,
  total: total.value,
  showSizeChanger: true,
  showTotal: (count) => `共 ${count} 条`,
}));

onMounted(loadTemplates);

async function loadTemplates() {
  loading.value = true;
  try {
    const page = await fetchPromptTemplates(query);
    templates.value = page.records;
    total.value = page.total;
    query.pageNum = page.pageNum;
    query.pageSize = page.pageSize;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载 Prompt 模板失败');
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.pageNum = 1;
  loadTemplates();
}

function resetSearch() {
  query.keyword = '';
  query.category = undefined;
  query.status = undefined;
  query.pageNum = 1;
  loadTemplates();
}

function handleTableChange(page: TablePaginationConfig) {
  query.pageNum = page.current || 1;
  query.pageSize = page.pageSize || 10;
  loadTemplates();
}

function openCreate() {
  editingId.value = null;
  Object.assign(form, {
    name: '通用运维排障模板',
    category: undefined,
    content: defaultContent,
    version: '1.0.0',
    defaultTemplate: false,
    enabled: true,
  });
  formOpen.value = true;
}

async function openEdit(id: number) {
  try {
    const detail = await fetchPromptTemplate(id);
    editingId.value = id;
    Object.assign(form, {
      name: detail.name,
      category: detail.category,
      content: detail.content,
      version: detail.version,
      defaultTemplate: detail.defaultTemplate,
      enabled: detail.enabled,
    });
    formOpen.value = true;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载模板详情失败');
  }
}

async function saveTemplate() {
  if (!form.name || !form.content) {
    message.warning('请填写模板名称和内容');
    return;
  }
  saving.value = true;
  try {
    const payload: PromptTemplatePayload = {
      name: form.name,
      category: form.category,
      content: form.content,
      version: form.version || '1.0.0',
      defaultTemplate: form.defaultTemplate,
      status: form.enabled ? 1 : 0,
    };
    if (editingId.value) {
      await updatePromptTemplate(editingId.value, payload);
      message.success('Prompt 模板已保存');
    } else {
      await createPromptTemplate(payload);
      message.success('Prompt 模板已创建');
    }
    formOpen.value = false;
    await loadTemplates();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '保存 Prompt 模板失败');
  } finally {
    saving.value = false;
  }
}

async function changeStatus(id: number, enabled: boolean) {
  try {
    await updatePromptTemplateStatus(id, enabled ? 1 : 0);
    message.success('状态已更新');
    await loadTemplates();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '状态更新失败');
  }
}

async function setDefault(id: number) {
  try {
    await setDefaultPromptTemplate(id);
    message.success('默认模板已更新');
    await loadTemplates();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '设置默认模板失败');
  }
}

async function removeTemplate(id: number) {
  try {
    await deletePromptTemplate(id);
    message.success('Prompt 模板已删除');
    await loadTemplates();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除 Prompt 模板失败');
  }
}

function insertVariable(variable: string) {
  form.content = `${form.content}${form.content.endsWith('\n') ? '' : '\n'}${variable}`;
}
</script>
