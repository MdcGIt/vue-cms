<template>
  <div class="app-container">
    <el-row :gutter="20">
      <el-col :span="4" :xs="24">
        <div class="head-container">
          <el-input
            v-model="statName"
            clearable
            size="small"
            prefix-icon="el-icon-search"
            style="margin-bottom: 20px"
          />
        </div>
        <div class="head-container">
          <el-tree
            :data="statOptions"
            :props="defaultProps"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            ref="tree"
            node-key="type"
            default-expand-all
            highlight-current
            @node-click="handleNodeClick"
          />
        </div>
      </el-col>
      <el-col :span="20" :xs="24">
        
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { listStatTypes } from "@/api/stat/stat";
import Treeselect from "@riophae/vue-treeselect";
import "@riophae/vue-treeselect/dist/vue-treeselect.css";

export default {
  name: "StatIndex",
  components: { Treeselect },
  data() {
    return {
      loading: true,
      statOptions: undefined,
      statType: undefined,
      queryParams: {}
    };
  },
  watch: {
    // 根据名称筛选部门树
    statName(val) {
      this.$refs.tree.filter(val);
    }
  },
  created() {
    this.getStatType();
  },
  methods: {
    getStatType() {
      listStatTypes().then(response => {
        this.statOptions = response.data;
      });
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.label.indexOf(value) !== -1;
    },
    handleNodeClick(data) {
      this.statType = data.type;
    }
  }
};
</script>