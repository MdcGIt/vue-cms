<template>
  <div id="catalog-selector">
    <el-dialog title="选择栏目"
              :visible.sync="visible"
              width="450px"
              :close-on-click-modal="false"
              append-to-body
              class="catalog-selector"
              style="padding: 10px 20x;">
      <div v-if="showToolbar" class="header-toolbar">
        <div v-if="showCopyToolbar">
          <el-radio-group v-model="copyType" size="mini">
            <el-radio-button label="1">拷贝副本</el-radio-button>
            <el-radio-button label="2">映射内容</el-radio-button>
          </el-radio-group>
          <el-tooltip placement="right" style="margin-left:5px;">
            <div slot="content">
              拷贝副本：完整复制内容，独立于来源自由修改。<br/>
              映射内容：仅生成基础信息，独立页面，扩展内容共享自源内容。
            </div>
            <i class="el-icon-info"></i>
          </el-tooltip>
        </div>
      </div>
      <div class="search-toolbar">
        <el-input placeholder="输入栏目名称"
                  v-model="filterCatalogName"
                  clearable
                  size="small"
                  suffix-icon="el-icon-search">
        </el-input>
      </div>
      <div class="tree-container">
        <el-scrollbar style="height: 400px;">
          <el-tree :data="catalogOptions" 
                  :props="defaultProps" 
                  :expand-on-click-node="false"
                  :filter-node-method="filterNode"
                  :show-checkbox="multiple"
                  :check-strictly="checkStrictly"
                  v-loading="loading"
                  node-key="id"
                  ref="tree"
                  default-expand-all
                  highlight-current
                  @node-click="handleNodeClick">
          </el-tree>
        </el-scrollbar>
      </div>
      <div slot="footer"
            class="dialog-footer">
        <el-button type="primary" 
                    @click="handleOk">确 定</el-button>
        <el-button @click="handleCancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getCatalogTreeData } from "@/api/contentcore/catalog";

export default {
  name: "CMSCatalogSelector",
  props: {
    open: {
      type: Boolean,
      default: false,
      required: true
    },
    // 是否显示复制内容工具栏
    showCopyToolbar: {
      type: Boolean,
      default: false,
      required: false
    },
    // 是否显示站点切换工具栏
    showSiteToolbar: {
      type: Boolean,
      default: false,
      required: false
    },
    // 是否多选
    multiple: {
      type: Boolean,
      default: false,
      required: false
    },
    checkStrictly: {
      type: Boolean,
      default: true,
      required: false
    }
  },
  computed: {
    showToolbar() {
      return this.showCopyToolbar || this.showSiteToolbar;
    }
  },
  data () {
    return {
      loading: false,
      visible: this.open,
      // 栏目树过滤：栏目名称
      filterCatalogName: undefined,
      // 栏目树数据
      catalogOptions: undefined,
      // 站点名称
      siteName: undefined,
      // 当前选中栏目ID
      selectedCatalogs: [],
      copyType: 1,
      defaultProps: {
        children: "children",
        label: "label"
      }
    };
  },
  watch: {
    open () {
      this.visible = this.open;
    },
    visible (newVal) {
      if (!newVal) {
        this.handleCancel();
      } else {
        this.loadCatalogTreeData();
      }
    },
    filterCatalogName(val) {
      this.$refs.tree.filter(val);
    }
  },
  methods: {
    loadCatalogTreeData () {
      this.loading = true;
      getCatalogTreeData().then(response => {
        if (response.code == 200) {
          this.catalogOptions = response.data.rows;
          this.siteName = response.data.siteName; 
        }
        this.loading = false;
      });
    },
    filterNode (value, data) {
      if (!value) return true;
      return data.label.indexOf(value) > -1;
    },
    handleNodeClick (data) {
      if (!this.multiple) {
        this.selectedCatalogs = [{ id: data.id, name: data.label, props: data.props }];
      }
    },
    handleOk (data) {
      if (this.multiple) {
        this.selectedCatalogs = [];
        this.$refs.tree.getCheckedNodes().map(item => {
          this.selectedCatalogs.push({ id: item.id, name: item.label, props: data.props });
        })
      }
      if (this.selectedCatalogs.length == 0) {
        this.$modal.alertWarning("请选择栏目");
        return;
      }
      this.$emit("ok", this.selectedCatalogs, this.copyType);
    },
    handleCancel () {
      this.$emit("close");
      this.selectedCatalogs = [];
      this.copyType = 1;
    }
  }
};
</script>
<style>
.catalog-selector .el-dialog__body {
  padding: 10px 20px;
}
.catalog-selector .header-toolbar {
  margin-bottom: 10px;
}
.catalog-selector .tree-container {
  margin: 10px 0;
}
.catalog-selector .tree-container .el-tree {
  height: 400px;
}
.catalog-selector .tree-container .el-scrollbar__wrap {
  overflow-x: hidden;
}
</style>