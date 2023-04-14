<template>
  <div class="menu-perms-container" v-loading="loading">
    <el-row class="mb12">
      <el-col>
        <el-button plain type="success" icon="el-icon-edit" size="mini" @click="handleSave">{{ $t("Common.Save") }}</el-button>
        <el-button plain type="primary" icon="el-icon-check" size="mini" @click="handleSelectAll">{{ this.selectAll ? $t('Common.CheckInverse') : $t('Common.CheckAll')  }}</el-button>
      </el-col>
    </el-row>
    <el-row>
      <el-col>
        <el-table 
          v-loading="loading"
          :data="sitePrivs"
          style="width:100%;line-height: normal;">
          <el-table-column type="index" :label="$t('Common.RowNo')" width="50">
          </el-table-column>
          <el-table-column :label="$t('CMS.Site.Name')" width="200">
            <template slot-scope="scope">
                <el-checkbox @change="handleRowSelectAll($event, scope.row.siteId)" v-model="scope.row.perms['View']">{{ scope.row.name }}</el-checkbox>
            </template>
          </el-table-column>
          <template v-for="(item, index) in sitePrivItems">
            <el-table-column :key="index" :label="item.name" v-if="item.id!='View'">
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
import { getSitePermissions, saveSitePermissions } from "@/api/contentcore/perms"

export default {
  name: "SitePermission",
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
      selectColumnAll: {},
      sitePrivs: [],
      sitePrivItems: []
    };
  }, 
  methods: {
    loadData() {
      this.loading = true;
      const params = { ownerType: this.ownerType, owner: this.owner }
      getSitePermissions(params).then(response => {
        this.sitePrivs = response.data.sitePrivs;
        this.sitePrivItems = response.data.sitePrivItems;
        this.loading = false;
      });
    },
    handleSelectAll() {
      this.selectAll = !this.selectAll;
      this.sitePrivs.forEach(row => {
        Object.keys(row.perms).forEach(key => row.perms[key] = this.selectAll)
      })
    },
    handleRowSelectAll(value, siteId) {
      this.sitePrivs.some(row => {
        if (row.siteId == siteId) {
          Object.keys(row.perms).forEach(key => {
            row.perms[key] = value;
          })
          return true;
        }
        return false;
      })
    },
    handleColumnSelectAll(column) {
      this.selectColumnAll[column] = !this.selectColumnAll[column];
      this.sitePrivs.forEach(row => {
        Object.keys(row.perms).forEach(key => {
          if (key == column) {
            row.perms[key] = this.selectColumnAll[column];
            if (this.selectColumnAll[column]) {
              row.perms['View'] = this.selectColumnAll[column]
            }
          }
        })
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
        perms: this.sitePrivs
      };
      saveSitePermissions(data).then(response => {
        this.$modal.msgSuccess(this.$t("Common.SaveSuccess"));
      });
    }
  }
};
</script>
