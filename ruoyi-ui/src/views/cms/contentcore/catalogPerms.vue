<template>
  <div class="menu-perms-container" v-loading="loading">
    <el-row :gutter="10" class="mb10">
      <el-col :span="1.5">
        <el-select v-model="currentSiteId" size="mini" @change="handleSiteChange">
          <el-option
            v-for="item in siteOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-col>
      <el-col :span="1.5">
        <el-button plain type="success" icon="el-icon-edit" size="mini" @click="handleSave">保存</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button plain type="primary" icon="el-icon-check" size="mini" @click="handleSelectAll">{{ this.selectAll ? '全不选' : '全选' }}</el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col>
        <el-table 
          v-loading="loading"
          :data="catalogPrivs"
          :height="tableHeight"
          :max-height="tableMaxHeight"
          row-key="catalogId"
          default-expand-all
          style="width:100%;line-height: normal;">
          <el-table-column label="栏目名称" width="200">
            <template slot-scope="scope">
                <el-checkbox @change="handleRowSelectAll($event, scope.row.catalogId)" v-model="scope.row.perms['View']">{{ scope.row.name }}</el-checkbox>
            </template>
          </el-table-column>
          <template v-for="(item, index) in catalogPrivItems">
            <el-table-column :key="index" :label="item.name" v-if="item.id!='View'" width="100">
              <template slot="header">
                <el-checkbox @change="handleColumnSelectAll(item.id)">{{ item.name }}</el-checkbox>
              </template>
              <template slot-scope="scope">
                <el-checkbox v-model="scope.row.perms[item.id]" @change="handleRowColumnChange($event, scope.row)"></el-checkbox>
              </template>
            </el-table-column>
          </template>
        </el-table>
      </el-col>
    </el-row>
  </div>
</template>
<script>
import { getSiteOptions } from "@/api/contentcore/site"
import { getCatalogPermissions, saveCatalogPermissions } from "@/api/contentcore/perms"

export default {
  name: "CatalogPermission",
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
  data() {
    return {
      loading: true,
      tableHeight: 600,
      tableMaxHeight: 900,
      selectAll: false,
      selectColumnAll: {},
      catalogPrivs: [],
      catalogPrivItems: [],
      currentSiteId: "",
      siteOptions: []
    };
  }, 
  created () {
    this.changeTableHeight();
    this.loadData();
    this.loadSiteOptions();
  },
  methods: {
    changeTableHeight () {
      let height = document.body.offsetHeight // 网页可视区域高度
      this.tableHeight = height - 140;
      this.tableMaxHeight = this.tableHeight;
    },
    loadSiteOptions() {
      getSiteOptions().then(response => {
        this.siteOptions = response.data.rows;
      })
    },
    loadData() {
      this.loading = true;
      const params = { ownerType: this.ownerType, owner: this.owner, siteId: this.currentSiteId }
      getCatalogPermissions(params).then(response => {
        this.currentSiteId = response.data.siteId;
        this.catalogPrivs = response.data.catalogPrivs;
        this.catalogPrivItems = response.data.catalogPrivItems;
        this.loading = false;
      });
    },
    handleSiteChange() {
      this.loadData();
    },
    handleSelectAll() {
      this.selectAll = !this.selectAll;
      this.selectCatalogPrivs(this.catalogPrivs, this.selectAll)
    },
    selectCatalogPrivs(arr, checked) {
      arr.forEach(row => {
        Object.keys(row.perms).forEach(key => row.perms[key] = checked)
        if (row.children && row.children.length > 0) {
          this.selectCatalogPrivs(row.children, checked)
        }
      });
    },
    handleRowSelectAll(value, catalogId) {
      this.selectRowAll(this.catalogPrivs, value, catalogId)
    },
    selectRowAll(arr, value, catalogId) {
      arr.some(row => {
        if (row.catalogId == catalogId) {
          Object.keys(row.perms).forEach(key => {
            row.perms[key] = value;
          })
          return true;
        }
        if (row.children && row.children.length > 0) {
          this.selectRowAll(row.children, value, catalogId)
        }
        return false
      })
    },
    handleColumnSelectAll(column) {
      this.selectColumnAll[column] = !this.selectColumnAll[column];
      this.selectColumn(this.catalogPrivs, column, this.selectColumnAll[column]);
    },
    selectColumn(arr, column, checked) {
      arr.forEach(row => {
        Object.keys(row.perms).forEach(key => {
          if (key == column) {
            row.perms[key] = checked;
            if (checked) {
              row.perms['View'] = checked
            }
          }
        })
        if (row.children && row.children.length > 0) {
          this.selectColumn(row.children, column, checked)
        }
      })
    },
    handleRowColumnChange(value, row) {
      if (value) {
        row.perms['View'] = true;
      }
    },
    handleSave() {
      const data = {
        ownerType: this.ownerType,
        owner: this.owner,
        siteId: this.siteId,
        perms: this.catalogPrivs
      };
      saveCatalogPermissions(data).then(response => {
        this.$modal.msgSuccess("保存成功");
      });
    },
    getPermissionKeys(arr, permissions) {
      arr.forEach(row => {
        Object.keys(row.perms).forEach(key => {
          if (row.perms[key]) {
            permissions.push(key + ":" + row.catalogId);
          }
        })
        if (row.children && row.children.length > 0) {
          this.getPermissionKeys(row.children, permissions)
        }
      })
    }
  }
};
</script>
