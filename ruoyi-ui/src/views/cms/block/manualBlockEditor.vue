<template>
  <div class="app-container block-manual-container">
    <div class="mb10">
      <el-button 
        plain
        type="success"
        icon="el-icon-edit"
        size="mini"
        @click="handleSave">{{ $t("Common.Save") }}</el-button>
      <el-button 
        plain
        type="primary"
        icon="el-icon-s-promotion"
        size="mini"
        @click="handlePublish">发布</el-button>
      <el-button 
        plain
        type="primary"
        icon="el-icon-view"
        size="mini"
        @click="handlePreview">预览</el-button>
    </div>
    <el-form 
      ref="form"
      :model="form"
      :rules="rules"
      label-width="80px">
      <el-card shadow="hover">
        <div slot="header" class="clearfix">
          <span>基础属性</span>
        </div>
        <div class="form-col">
          <el-form-item label="名称"
                        prop="name">
            <el-input v-model="form.name" />
          </el-form-item>
          <el-form-item label="编码"
                        prop="code">
            <el-input v-model="form.code" />
          </el-form-item>
          <el-form-item label="发布目录"
                        prop="path">
            <el-input v-model="form.path" />
          </el-form-item>
          <el-form-item label="发布通道"
                        prop="publishPipeCode">
            <el-select v-model="form.publishPipeCode">
              <el-option
                v-for="pp in publishPipes"
                :key="pp.pipeCode"
                :label="pp.pipeName"
                :value="pp.pipeCode"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="模板"
                        prop="template">
            <el-input v-model="form.template" :disabled="templateDisabled" >
            <el-button slot="append"
                      type="primary"
                      :disabled="templateDisabled"
                      @click="handleSelectTemplate()">{{ $t("Common.Select") }}</el-button>
          </el-input>
          </el-form-item>
          <el-form-item :label="$t('Common.Remark')"
                        prop="remark">
            <el-input v-model="form.remark" />
          </el-form-item>
        </div>
      </el-card>
    </el-form>
    <el-card class="mt10">
      <div slot="header" class="clearfix">
        <span>自定义列表</span>
      </div>
      <el-table :data="this.form.content" style="width: 100%">
        <el-table-column
          type="index"
          label="序号"
          width="50">
        </el-table-column>
        <el-table-column
          label="标题"
          prop="title">
          <template slot-scope="scope">
            <span class="row-insert">
              <el-button 
                icon="el-icon-plus" 
                circle 
                size="mini"
                @click="handleAddItem(scope.$index)">
              </el-button>
            </span>
            <span class="row">
              <el-tag
                class="item-data"
                effect="plain"
                type="info"
                :key="item.title"
                v-for="(item, index) in scope.row.items">
                <el-link :underline="false" @click="handleEditItem(scope.$index, index)">{{item.title}}</el-link>
                <span class="item-op">
                  <el-link 
                    class="item-op-add"
                    :underline="false" 
                    icon="el-icon-circle-plus-outline" 
                    @click="handleAddItem(scope.$index, index + 1)">
                  </el-link>
                  <el-link 
                    class="item-op-del"
                    :underline="false" 
                    icon="el-icon-circle-close" 
                    @click="handleDeleteItem(scope.$index, index)">
                  </el-link>
                </span>
              </el-tag>
            </span>
          </template>
        </el-table-column>
        <el-table-column
          width="200"
          align="right">
          <template slot="header">
            <el-button
              plain
              type="primary"
              icon="el-icon-plus"
              size="mini"
              @click="handleAddRow(form.content.length)">添加行</el-button>
            <el-button
              plain
              type="danger"
              icon="el-icon-delete"
              size="mini"
              @click="handleClear">清空</el-button>
          </template>
          <template slot-scope="scope">
            <el-button
              plain
              size="mini"
              icon="el-icon-plus"
              @click="handleAddRow(scope.$index)">插入行</el-button>
            <el-button
              plain
              size="mini"
              icon="el-icon-delete"
              type="danger"
              @click="handleDeleteRow(scope.$index)">{{ $t("Common.Delete") }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <!-- 链接编辑弹窗 -->
    <el-dialog 
      :title="title"
      :visible.sync="dialogVisible"
      :close-on-click-modal="false"
      width="600px"
      append-to-body>
      <el-form 
        ref="form_item"
        :model="form_item"
        label-width="80px"
        class="form_item">
        <div class="form-row">
          <el-form-item 
            label="标题"
            prop="title">
            <el-input v-model="form_item.title">
              <el-dropdown slot="append" @command="handleLinkTo">
                <el-button size="mini" type="primary">
                  选择<i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="content">选择内容</el-dropdown-item>
                  <el-dropdown-item command="catalog">选择栏目</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </el-input>
          </el-form-item>
          <el-form-item label="链接"
                        prop="url">
            <el-input v-model="form_item.url" />
          </el-form-item>
          <el-form-item label="摘要"
                        prop="summary">
            <el-input type="textarea" v-model="form_item.summary" />
          </el-form-item>
          <el-form-item label="日期"
                        prop="date">
            <el-date-picker
              v-model="form_item.date"
              type="datetime"
              value-format="yyyy-MM-dd HH:mm:ss"
              placeholder="选择日期时间">
            </el-date-picker>
          </el-form-item>
          <el-form-item label="LOGO" prop="logo">
            <cms-logo-view v-model="form_item.logo" :src="form_item.logoSrc"
                            :width="218" :height="150"></cms-logo-view>
          </el-form-item>
        </div>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary"
                   @click="handleDialogOk">{{ $t("Common.Confirm") }}</el-button>
        <el-button @click="handleDialogClose">{{ $t("Common.Cancel") }}</el-button>
      </div>
    </el-dialog>
    <!-- 模板选择组件 -->
    <cms-template-selector :open="openTemplateSelector" 
                       :publishPipeCode="form.publishPipeCode"
                       @ok="handleTemplateSelected"
                       @cancel="handleTemplateSelectorCancel" />
    <!-- 栏目选择组件 -->
    <cms-catalog-selector
      :open="openCatalogSelector"
      @ok="handleCatalogSelectorOk"
      @close="handleCatalogSelectorClose"></cms-catalog-selector>
    <!-- 内容选择组件 -->
    <cms-content-selector
      :open="openContentSelector"
      @ok="handleContentSelectorOk"
      @close="handleContentSelectorClose"></cms-content-selector>
  </div>
</template>
<script>
import { getPublishPipeSelectData } from "@/api/contentcore/publishpipe";
import { getPageWidget, addPageWidget, editPageWidget, publishPageWidgets } from "@/api/contentcore/pagewidget";
import CMSLogoView from '@/views/cms/components/LogoView';
import CMSTemplateSelector from '@/views/cms/contentcore/templateSelector';
import CMSCatalogSelector from "@/views/cms/contentcore/catalogSelector";
import CMSContentSelector from "@/views/cms/contentcore/contentSelector";

export default {
  name: "CMSBlockManualEditor",
  components: {
    'cms-template-selector': CMSTemplateSelector,
    'cms-catalog-selector': CMSCatalogSelector,
    'cms-content-selector': CMSContentSelector,
    'cms-logo-view': CMSLogoView
  },
  data () {
    return {
      loading: false,
      pageWidgetId: this.$route.query.id,
      publishPipes: [],
      form: {
        publishPipeCode: '',
        content: []
      },
      rules: {
        name: [
          { required: true, message: "名称不能为空", trigger: "blur" }
        ],
        code: [
          { required: true, message: "编码不能为空", trigger: "blur" }
        ],
        publishPipeCode: [
          { required: true, message: "发布通道不能为空", trigger: "blur" }
        ],
        path: [
          { required: true, message: "目录不能为空", trigger: "blur" }
        ]
      },
      openTemplateSelector: false,
      title: "",
      dialogVisible: false,
      form_item: {},
      addItem: false,
      current: undefined,
      openCatalogSelector: false,
      openContentSelector: false
    };
  },
  computed: {
    templateDisabled() {
      return !this.form.publishPipeCode || this.form.publishPipeCode == null || this.form.publishPipeCode === '';
    }
  },
  created() {
    if (this.pageWidgetId) {
      this.loadPublishPipes();
      this.loadPageWidgetInfo();
    } else {
      this.$modal.msgError("参数异常");
    }
  },
  methods: {
    loadPublishPipes() {
      getPublishPipeSelectData().then(response => {
        this.publishPipes = response.data.rows;
      });
    },
    loadPageWidgetInfo() {
      getPageWidget(this.pageWidgetId).then(response => {
        if (response.code == 200) {
          this.form = response.data;
        }
      });
    },
    handleSave () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.form.catalogId = this.catalogId;
          this.form.contentStr = JSON.stringify(this.form.content);
          if (this.pageWidgetId) {
            editPageWidget(this.form).then(response => {
              if (response.code === 200) {
                this.$modal.msgSuccess(response.msg);
              }
            });
          } else {
            addPageWidget(this.form).then(response => {
              if (response.code === 200) {
                this.$modal.msgSuccess(response.msg);
                this.$router.push({ path: form.route, query: { id: response.data } });
              }
            });
          }
        }
      });
    },
    handleSelectTemplate () {
      this.openTemplateSelector = true;
    },
    handleTemplateSelected (template) {
      this.form.template = template;
      this.openTemplateSelector = false;
    },
    handleTemplateSelectorCancel () {
      this.openTemplateSelector = false;
    },
    handlePublish() {
      const pageWidgetIds = [ this.pageWidgetId ];
      publishPageWidgets(pageWidgetIds).then(response => {
        if (response.code === 200) {
          this.$modal.msgSuccess(response.msg);
        }
      });
    },
    handlePreview() {
      let routeData = this.$router.resolve({
        path: "/cms/preview",
        query: { type: "pagewidget", dataId: this.pageWidgetId },
      });
      window.open(routeData.href, '_blank');
    },
    handleAddRow(index) {
      this.form.content.splice(index, 0, {items:[]});
    },
    handleDeleteRow(index) {
      this.form.content.splice(index, 1);
    },
    handleAddItem(rowIndex, itemIndex) {
      this.title = "添加列表项";
      this.form_item = {};
      this.current = { row: rowIndex, col: itemIndex || 0 };
      this.addItem = true;
      this.dialogVisible = true;
    },
    handleDialogOk() {
      if (this.addItem) {
        this.form.content[this.current.row].items.splice(this.current.col, 0, this.form_item);
      } else {
        this.$set(this.form.content[this.current.row].items, this.current.col, this.form_item);
      }
      this.dialogVisible = false;
    },
    handleDialogClose() {
      this.dialogVisible = false;
    },
    handleEditItem(rowIndex, itemIndex) {
      this.current = { row: rowIndex, col: itemIndex };
      this.title = "编辑列表项";
      this.addItem = false;
      this.form_item = this.form.content[rowIndex].items[itemIndex];
      this.dialogVisible = true;
    },
    handleDeleteItem(rowIndex, itemIndex) {
      this.form.content[rowIndex].items.splice(itemIndex, 1);
    },
    handleClear() {
      this.form.content = [];
    },
    handleLinkTo(type) {
      if (type === 'content') {
        this.openContentSelector = true;
      } else if (type === 'catalog') {
        this.openCatalogSelector = true;
      }
    },
    handleContentSelectorOk(contents) {
      if (contents && contents.length > 0) {
        this.form_item.title = contents[0].title;
        this.form_item.logo = contents[0].logo;
        this.form_item.logoSrc = contents[0].logoSrc;
        this.form_item.publishDate = contents[0].publishDate;
        this.form_item.url = contents[0].internalUrl;
        this.form_item.summary = contents[0].summary;
        this.openContentSelector = false;
      } else {
        this.$modal.msgWarning("请先选择一条记录");
      }
    },
    handleContentSelectorClose() {
      this.openContentSelector = false;
    },
    handleCatalogSelectorOk(catalogs) {
      if (catalogs && catalogs.length > 0) {
        this.form_item.title = catalogs[0].name;
        this.form_item.logo = catalogs[0].props.logo;
        this.form_item.logoSrc = catalogs[0].props.logoSrc;
        this.form_item.url = catalogs[0].props.internalUrl;
        this.form_item.summary = catalogs[0].props.description;
      }
      this.openCatalogSelector = false;
    },
    handleCatalogSelectorClose(catalogs) {
      this.openCatalogSelector = false;
    },
  }
};
</script>
<style scoped>
.block-manual-container .item-data {
  margin-left: 5px ;
  border-radius: 15px;
}
.block-manual-container .row {
  line-height: 36px;
}
.block-manual-container .item-data .item-op .el-link {
  margin-left: 2px;
}
.block-manual-container .item-op-add {
  color: #1890ff;
}
.block-manual-container .item-op-del {
  color: #ff4949;
}
.block-manual-container .el-input, .el-select, .el-textarea {
  width: 400px;
}
.block-manual-container .form-row {
  width: 100%;
  display: inline-block;
}
.block-manual-container .el-form-item {
  width: 500px;
  float: left;
}
</style>