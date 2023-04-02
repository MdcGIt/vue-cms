<template>
  <div class="group-tree">
    <div class="head-container">
      <el-button type="text"
                 icon="el-icon-plus"
                 @click="handleAdd"
                 style="margin-top:2px;">新增分组</el-button>
      <el-input placeholder="输入分组名称"
                v-model="filterGroupName"
                clearable
                size="small"
                style="margin-top: 7px;"
                suffix-icon="el-icon-search">
      </el-input>
    </div>
    <div class="tree-container">
      <el-button type="text" 
                  class="tree-header"
                  icon="el-icon-s-home"
                  @click="handleTreeRootClick">{{ $t('APP.TITLE') }}</el-button>
      <el-tree :data="groupOptions" 
               :props="defaultProps" 
               :expand-on-click-node="false"
               :default-expanded-keys="treeExpandedKeys"
               :filter-node-method="filterNode"
               node-key="id"
               ref="tree"
               highlight-current
               @node-click="handleNodeClick">
        <span class="tree-node" slot-scope="{ node, data }">
          <span>{{ node.label }}</span>
          <span class="node-tool">
            <el-button
              type="text"
              size="mini"
              title="编辑"
              icon="el-icon-edit"
              @click="handleEdit(data)">
            </el-button>
            <el-button
              type="text"
              size="mini"
              title="删除"
              icon="el-icon-delete"
              @click="handleDelete(data)">
            </el-button>
          </span>
        </span>
      </el-tree>
    </div>
    <!-- 添加分组对话框 -->
    <el-dialog :title="diagTitle"
               :visible.sync="diagOpen"
               width="600px"
               append-to-body>
      <el-form ref="form"
               :model="form"
               :rules="rules"
               label-width="80px">
        <el-form-item label="上级分组"
                      prop="parentId">
          <treeselect v-model="form.parentId"
                      :options="groupOptions"
                      placeholder="选择上级分组" />
        </el-form-item>
        <el-form-item label="名称"
                      prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="唯一编码"
                      prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="图片" 
                      prop="logo">
          <cms-logo-view v-model="form.logo" :src="form.logoSrc"
                          :width="218" :height="150"></cms-logo-view>
        </el-form-item>
      </el-form>
      <div slot="footer"
           class="dialog-footer">
        <el-button type="primary"
                   @click="handleSave">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { getTagWordGroupTreeData, addTagWordGroup, editTagWordGroup, deleteTagWordGroup } from "@/api/word/tagWord";
import CMSLogoView from '@/views/cms/components/LogoView';
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "CMSTagWordGroupTree",
  components: {
    "cms-logo-view": CMSLogoView,
    Treeselect
  },
  computed: {
    theme() {
      return this.$store.state.settings.theme;
    }
  },
  data () {
    return {
      // 是否显示弹出层
      diagOpen: false,
      // 分组树过滤：名称
      filterGroupName: undefined,
      // 分组树数据
      groupOptions: undefined,
      // 当前选中分组ID
      selectedGroupId: undefined,
      treeExpandedKeys: [],
      defaultProps: {
        children: "children",
        label: "label"
      },
      diagTitle: "",
      form: {
      },
      rules: {
        name: [
          { required: true, message: "分组名称不能为空", trigger: "blur" }
        ],
        code: [
          { required: true, message: "分组编码不能为空", trigger: "blur" }
        ]
      }
    };
  },
  watch: {
    filterGroupName(val) {
      this.$refs.tree.filter(val);
    }
  },
  created () {
    this.loadGroupTreeData();
  },
  methods: {
    loadGroupTreeData () {
      getTagWordGroupTreeData().then(response => {
        this.groupOptions = response.data;
        this.$nextTick(() => {
          this.selectedGroupId = this.$cache.local.get("LastSelectedGroupId");
          if (this.selectedGroupId && this.$refs.tree) {
            this.$refs.tree.setCurrentKey(this.selectedGroupId);
            this.treeExpandedKeys = [this.selectedGroupId];
            this.$emit("node-click", this.$refs.tree.getCurrentNode());
          } else {
            this.$emit("node-click", null);
          }
        })
      });
    },
    // 筛选节点
    filterNode (value, data) {
      if (!value) return true;
      return data.label.indexOf(value) > -1;
    },
    // 根节点点击事件
    handleTreeRootClick() {
      this.selectedGroupId = undefined;
      this.$cache.local.remove("LastSelectedGroupId");
      this.$refs.tree.setCurrentKey(null);
      this.$emit("node-click", null);
    },
    // 节点单击事件
    handleNodeClick (data) {
      this.selectedGroupId = data.id;
      this.$cache.local.set("LastSelectedGroupId", this.selectedGroupId);
      this.$emit("node-click", data);
    },
    // 取消按钮
    cancel () {
      this.diagOpen = false;
      this.reset();
    },
    // 表单重置
    reset () {
      this.resetForm("form");
    },
    /** 新增按钮操作 */
    handleAdd () {
      this.form = {};
      this.form.parentId = this.selectedGroupId;
      this.diagTitle = '添加分组';
      this.diagOpen = true;
    },
    handleEdit (data) {
      this.form = { 
        groupId: data.id, 
        parentId: data.parentId == '0' ? null : data.parentId, 
        name: data.label, 
        code: data.props.code, 
        logo: data.props.logo, 
        logoSrc: data.props.logoSrc
      };
      this.diagTitle = '编辑分组';
      this.diagOpen = true;
    },
    /** 分组表单提交 */
    handleSave: function () {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (!this.selectedGroupId) {
            this.form.parentId = 0;
          }
          if (this.form.groupId) {
            editTagWordGroup(this.form).then(response => {
              this.diagOpen = false;
              this.$modal.msgSuccess(response.msg);
              this.loadGroupTreeData();
            });
          } else {
            addTagWordGroup(this.form).then(response => {
              this.$cache.local.set("LastSelectedGroupId", response.data.groupId);
              this.diagOpen = false;
              this.$modal.msgSuccess(response.msg);
              this.loadGroupTreeData();
            });
          }
        }
      });
    },
    handleDelete (data) {
      const groupIds = [ data.id ];
      this.$modal.confirm('是否确认删除选中的分组?').then(function() {
        return deleteTagWordGroup(groupIds);
      }).then((response) => {
        this.$modal.msgSuccess(response.msg);
        this.loadGroupTreeData();
      }).catch(() => {});
    }
  }
};
</script>
<style scoped>
.group-tree .tree-header {
  font-size: 16px;
  font-weight: 700;
  line-height: 22px;
  color: #424242;
  width: 100%;
  text-align: left;
  padding-left: 4px;
}
.group-tree-header:hover {
  background-color: #F5F7FA;
}
.group-tree .tree-node {
  width: 100%;
  line-height: 30px;
}
.group-tree .tree-node .node-tool {
  display: none;
  position: absolute;
  right: 5px;
}
.group-tree .tree-node:hover .node-tool {
  display: inline-block;
}
.group-tree .tree-node .node-tool .el-button {
  font-size: 14px;
}
</style>