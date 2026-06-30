<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1>知识库</h1>
        <p>维护 Markdown 运维知识，供后续 AI 排障优先参考。</p>
      </div>
      <a-space>
        <a-button @click="loadKnowledge">
          <reload-outlined />
          刷新
        </a-button>
        <a-button type="primary" @click="openCreate">
          <plus-outlined />
          新增知识
        </a-button>
      </a-space>
    </div>

    <a-card :bordered="false" class="filter-card">
      <a-form layout="inline">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" allow-clear placeholder="标题、正文或来源" @pressEnter="handleSearch" />
        </a-form-item>
        <a-form-item label="分类">
          <a-select v-model:value="query.category" allow-clear :options="categoryOptions" class="filter-select" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear :options="statusOptions" class="filter-select" />
        </a-form-item>
        <a-form-item label="标签">
          <a-input v-model:value="query.tag" allow-clear placeholder="标签关键词" @pressEnter="handleSearch" />
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
        :data-source="items"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'title'">
            <a-button type="link" class="table-link" @click="openDetail(record.id)">
              {{ record.title }}
            </a-button>
            <div class="muted-line">{{ record.sourceType }} · {{ record.sourceRef || 'manual' }}</div>
          </template>
          <template v-else-if="column.key === 'tags'">
            <a-space wrap>
              <a-tag v-for="tag in record.tags" :key="tag">{{ tag }}</a-tag>
              <span v-if="!record.tags?.length" class="muted-line">-</span>
            </a-space>
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
              <a-button size="small" @click="openDetail(record.id)">查看</a-button>
              <a-button size="small" @click="openEdit(record.id)">编辑</a-button>
              <a-popconfirm title="确认删除这条知识？" @confirm="removeKnowledge(record.id)">
                <a-button size="small" danger>删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-drawer
      v-model:open="detailOpen"
      width="min(960px, 100vw)"
      :title="detail?.title || '知识详情'"
      :destroy-on-close="true"
    >
      <a-spin :spinning="detailLoading">
        <a-empty v-if="!detail" description="暂无详情" />
        <div v-else class="detail-content">
          <a-descriptions bordered size="small" :column="2">
            <a-descriptions-item label="分类">{{ detail.category || '-' }}</a-descriptions-item>
            <a-descriptions-item label="状态">{{ detail.enabled ? '启用' : '停用' }}</a-descriptions-item>
            <a-descriptions-item label="来源">{{ detail.sourceType }} / {{ detail.sourceRef || '-' }}</a-descriptions-item>
            <a-descriptions-item label="版本">{{ detail.version }}</a-descriptions-item>
            <a-descriptions-item label="标签" :span="2">
              <a-space wrap>
                <a-tag v-for="tag in detail.tags" :key="tag">{{ tag }}</a-tag>
                <span v-if="!detail.tags?.length">-</span>
              </a-space>
            </a-descriptions-item>
          </a-descriptions>

          <a-tabs class="section">
            <a-tab-pane key="preview" tab="预览">
              <div class="markdown-preview" v-html="renderMarkdown(detail.content)" />
            </a-tab-pane>
            <a-tab-pane key="source" tab="Markdown">
              <pre class="message-content">{{ detail.content }}</pre>
            </a-tab-pane>
          </a-tabs>
        </div>
      </a-spin>
    </a-drawer>

    <a-modal
      v-model:open="formOpen"
      :title="editingId ? '编辑知识' : '新增知识'"
      :confirm-loading="saving"
      width="900px"
      @ok="saveKnowledge"
    >
      <a-form layout="vertical">
        <a-form-item label="标题" required>
          <a-input v-model:value="form.title" placeholder="例如：Docker permission denied 排查手册" />
        </a-form-item>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="分类">
              <a-select v-model:value="form.category" allow-clear :options="categoryOptions" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="版本">
              <a-input v-model:value="form.version" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="标签">
              <a-select
                v-model:value="form.tags"
                mode="tags"
                :token-separators="[',', '，']"
                placeholder="输入标签后回车"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="来源类型">
              <a-input v-model:value="form.sourceType" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="来源引用">
              <a-input v-model:value="form.sourceRef" placeholder="例如：case:1001 或 manual" />
            </a-form-item>
          </a-col>
          <a-col :span="4">
            <a-form-item label="状态">
              <a-switch v-model:checked="form.enabled" checked-children="启用" un-checked-children="停用" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="正文 Markdown" required>
          <a-textarea v-model:value="form.content" :rows="14" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message, type TablePaginationConfig } from 'ant-design-vue';
import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons-vue';
import { defaultCategoryOptions, fetchFaultCategoryOptions } from '@/api/fault-category';
import {
  createKnowledge,
  deleteKnowledge,
  fetchKnowledge,
  fetchKnowledgeList,
  updateKnowledge,
  updateKnowledgeStatus,
  type KnowledgeDetail,
  type KnowledgePayload,
  type KnowledgeSummary,
} from '@/api/knowledge';

const loading = ref(false);
const saving = ref(false);
const detailLoading = ref(false);
const detailOpen = ref(false);
const formOpen = ref(false);
const editingId = ref<number | null>(null);
const items = ref<KnowledgeSummary[]>([]);
const detail = ref<KnowledgeDetail | null>(null);
const total = ref(0);

const query = reactive({
  keyword: '',
  category: undefined as string | undefined,
  status: undefined as number | undefined,
  tag: '',
  pageNum: 1,
  pageSize: 10,
});

const form = reactive({
  title: '',
  category: undefined as string | undefined,
  tags: [] as string[],
  content: '',
  sourceType: 'MANUAL',
  sourceRef: '',
  version: '1.0.0',
  enabled: true,
});

const categoryOptions = ref(defaultCategoryOptions);

const statusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
];

const columns = [
  { title: '标题', key: 'title', dataIndex: 'title' },
  { title: '分类', key: 'category', dataIndex: 'category', width: 130 },
  { title: '标签', key: 'tags', dataIndex: 'tags', width: 220 },
  { title: '版本', key: 'version', dataIndex: 'version', width: 100 },
  { title: '状态', key: 'enabled', dataIndex: 'enabled', width: 120 },
  { title: '更新时间', key: 'updatedAt', dataIndex: 'updatedAt', width: 190 },
  { title: '操作', key: 'action', width: 190 },
];

const pagination = computed<TablePaginationConfig>(() => ({
  current: query.pageNum,
  pageSize: query.pageSize,
  total: total.value,
  showSizeChanger: true,
  showTotal: (count) => `共 ${count} 条`,
}));

onMounted(async () => {
  await Promise.all([loadKnowledge(), loadCategoryOptions()]);
});

async function loadCategoryOptions() {
  try {
    const options = await fetchFaultCategoryOptions();
    categoryOptions.value = options.map((item) => ({ label: item.categoryName, value: item.categoryName }));
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载故障分类失败');
  }
}

async function loadKnowledge() {
  loading.value = true;
  try {
    const page = await fetchKnowledgeList({
      ...query,
      tag: query.tag || undefined,
    });
    items.value = page.records;
    total.value = page.total;
    query.pageNum = page.pageNum;
    query.pageSize = page.pageSize;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载知识库失败');
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.pageNum = 1;
  loadKnowledge();
}

function resetSearch() {
  query.keyword = '';
  query.category = undefined;
  query.status = undefined;
  query.tag = '';
  query.pageNum = 1;
  loadKnowledge();
}

function handleTableChange(page: TablePaginationConfig) {
  query.pageNum = page.current || 1;
  query.pageSize = page.pageSize || 10;
  loadKnowledge();
}

async function openDetail(id: number) {
  detailOpen.value = true;
  detailLoading.value = true;
  detail.value = null;
  try {
    detail.value = await fetchKnowledge(id);
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载知识详情失败');
  } finally {
    detailLoading.value = false;
  }
}

function openCreate() {
  editingId.value = null;
  Object.assign(form, {
    title: '',
    category: undefined,
    tags: [],
    content: '',
    sourceType: 'MANUAL',
    sourceRef: '',
    version: '1.0.0',
    enabled: true,
  });
  formOpen.value = true;
}

async function openEdit(id: number) {
  try {
    const item = await fetchKnowledge(id);
    editingId.value = id;
    Object.assign(form, {
      title: item.title,
      category: item.category,
      tags: item.tags || [],
      content: item.content,
      sourceType: item.sourceType || 'MANUAL',
      sourceRef: item.sourceRef || '',
      version: item.version || '1.0.0',
      enabled: item.enabled,
    });
    formOpen.value = true;
  } catch (error) {
    message.error(error instanceof Error ? error.message : '加载知识失败');
  }
}

async function saveKnowledge() {
  if (!form.title.trim() || !form.content.trim()) {
    message.warning('请填写标题和正文');
    return;
  }
  saving.value = true;
  try {
    const payload: KnowledgePayload = {
      title: form.title.trim(),
      category: form.category,
      tags: form.tags || [],
      content: form.content,
      contentType: 'MARKDOWN',
      sourceType: form.sourceType || 'MANUAL',
      sourceRef: form.sourceRef || undefined,
      version: form.version || '1.0.0',
      status: form.enabled ? 1 : 0,
    };
    if (editingId.value) {
      await updateKnowledge(editingId.value, payload);
      message.success('知识已保存');
    } else {
      await createKnowledge(payload);
      message.success('知识已创建');
    }
    formOpen.value = false;
    await loadKnowledge();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '保存知识失败');
  } finally {
    saving.value = false;
  }
}

async function changeStatus(id: number, enabled: boolean) {
  try {
    await updateKnowledgeStatus(id, enabled ? 1 : 0);
    message.success('状态已更新');
    await loadKnowledge();
    if (detail.value?.id === id) {
      detail.value = await fetchKnowledge(id);
    }
  } catch (error) {
    message.error(error instanceof Error ? error.message : '状态更新失败');
  }
}

async function removeKnowledge(id: number) {
  try {
    await deleteKnowledge(id);
    message.success('知识已删除');
    if (detail.value?.id === id) {
      detailOpen.value = false;
      detail.value = null;
    }
    await loadKnowledge();
  } catch (error) {
    message.error(error instanceof Error ? error.message : '删除知识失败');
  }
}

function renderMarkdown(markdown: string) {
  return escapeHtml(markdown || '')
    .replace(/^### (.*)$/gm, '<h3>$1</h3>')
    .replace(/^## (.*)$/gm, '<h2>$1</h2>')
    .replace(/^# (.*)$/gm, '<h1>$1</h1>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br />');
}

function escapeHtml(value: string) {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}
</script>
