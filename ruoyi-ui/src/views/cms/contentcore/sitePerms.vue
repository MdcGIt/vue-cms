<template>
  <div class="menu-perms-container" v-loading="loading">
    <el-row class="mb10">
      <el-col>
        <el-button plain type="success" icon="el-icon-edit" size="mini" @click="handleSave">保存</el-button>
        <el-button plain type="primary" icon="el-icon-check" size="mini" @click="handleSelectAll">{{ this.selectAll ? '全不选' : '全选' }}</el-button>
        <el-button plain type="primary" icon="el-icon-bottom-right" size="mini" @click="handleExpandAll">{{ this.expandAll ? '收起' : '展开' }}</el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col>
        <el-table 
          v-loading="loading"
          :data="siteList"
          style="width:100%;line-height: normal;">
          <el-table-column label="ID"
                            width="200"
                            prop="siteId" />
          <el-table-column label="站点名称">
            <template slot-scope="scope">
              <el-link type="primary"
                        @click="handleEdit(scope.row)"
                        class="link-type">
                <span>{{ scope.row.name }}</span>
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="目录名"
                            width="200"
                            prop="path" />
          <el-table-column label="操作"
                            align="center"
                            width="300" 
                            class-name="small-padding fixed-width">
            <template slot-scope="scope">
              <el-button size="mini"
                          type="text"
                          icon="el-icon-s-promotion"
                          @click="handlePublish(scope.row)">发布首页</el-button>
              <el-button size="mini"
                          type="text"
                          @click="handlePreview(scope.row)"><svg-icon icon-class="eye-open" class="mr1"></svg-icon>预览</el-button>
              <el-button size="mini"
                          type="text"
                          icon="el-icon-edit"
                          @click="handleEdit(scope.row)">修改</el-button>
              <el-button size="mini"
                          type="text"
                          icon="el-icon-delete"
                          @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>
  </div>
</template>
<script>
import { savePermissions, getMenuPerms } from "@/api/system/perms";

export default {
  name: "MenuPermission",
  props: {
    ownerType: {
      type: String,
      require: false,
      default: ""
    },
    owner: {
      type: String,
      require: false,
      default: ""
    }
  },
  watch: {
    ownerType: {
      handler(newV, oldV) {
        if (newV && newV != '') {
          this.form.ownerType = newV;
          this.loadData();
        }
      }
    },
    owner: {
      handler(newV, oldV) {
        if (newV && newV != '') {
          this.form.owner = newV;
          this.loadData();
        }
      }
    },
  },
  mounted() {
    this.loadData();
  },
  data() {
    return {
      loading: true,
      selectAll: false,
      expandAll: true,
      menuTreeData: [],
      perms: [],
      form: {},
      defaultProps: {
        children: 'children',
        label: 'menuName'
      }
    };
  }, 
  methods: {
    loadData() {
      this.loading = true;
      const params = { ownerType: this.ownerType, owner: this.owner }
      getMenuPerms(params).then(response => {
        this.menuTreeData = this.handleTree(response.data.menus, '0', "menuId");
        this.perms = response.data.perms;
        this.$nextTick(() => {
          this.setCheckedMenu(this.menuTreeData);
          this.loading = false;
        })
      });
    },
    setCheckedMenu(menus) {
      menus.forEach(m => {
        if (this.perms.includes(m.perms)) {
          this.$refs.menu.setChecked(m.menuId, true);
        }
        if (m.children && m.children.length > 0) {
          this.setCheckedMenu(m.children);
        }
      });
    },
    handleTreeNodeCheckChange(node, checked) {
      if (node.perms && node.perms.length > 0) {
        if (checked) {
            this.perms.push(node.perms);
        } else {
          for(let i = 0; i < this.perms.length; i++) {
            if(this.perms[i] == node.perms) {
              this.perms.splice(i, 1);
              break;
            }
          }
        }
      }
    },
    handleSelectAll() {
      this.$refs.menu.setCheckedNodes(!this.selectAll ? this.menuTreeData : []);
      this.selectAll = !this.selectAll;
    },
    handleExpandAll() {
      let treeList = this.menuTreeData;
      for (let i = 0; i < treeList.length; i++) {
        this.$refs.menu.store.nodesMap[treeList[i].menuId].expanded = !this.expandAll;
      }
      this.expandAll = !this.expandAll;
    },
    getMenuAllCheckedKeys() {
      let checkedKeys = this.$refs.menu.getCheckedNodes();
      let halfCheckedKeys = this.$refs.menu.getHalfCheckedNodes();
      checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys);
      return checkedKeys;
    },
    handleSave() {
      let permissions = [];
      this.getMenuAllCheckedKeys().forEach(node => {
        if (node.perms && node.perms != '') {
          permissions.push(node.perms);
        }
      });
      const data = {
        ownerType: this.ownerType,
        owner: this.owner,
        permType: 'Menu',
        permissions: permissions
      };
      savePermissions(data).then(response => {
        this.$modal.msgSuccess("保存成功");
      });
    }
  }
};
</script>
