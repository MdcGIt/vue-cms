<template>
  <div class="app-container cms-template-tag">
    <el-row>
      <el-form size="small"
               :inline="true">
        <el-form-item>
          <el-input v-model="tagName"
                    placeholder="请输入标签名"
                    clearable
                    style="width: 200px"
                    @input="handleQuery" />
        </el-form-item>
      </el-form>
    </el-row>
    <el-row>
      <el-table v-loading="loading" 
                ref="tagListTable" 
                :data="tagList"
                @row-click="handleRowClick">
        <el-table-column type="expand">
          <template slot-scope="scope">
            <el-row v-if="scope.row.description!=''" style="padding:0 20px;">
              <el-descriptions title="用法描述：" :colon="false">
                <el-descriptions-item :contentStyle="{width:'100%'}">{{ scope.row.description }}</el-descriptions-item>
              </el-descriptions>
              <el-descriptions title="标签属性：" :colon="false">
                <el-descriptions-item :contentStyle="{width:'100%'}">
                  <el-table :data="scope.row.tagAttrs" border>
                    <el-table-column label="属性名"
                                    align="center"
                                    width="150"
                                    prop="name" />
                    <el-table-column label="数据类型"
                                    align="center"
                                    width="100"
                                    prop="dataType" />
                    <el-table-column label="是否必填"
                                    align="center"
                                    width="80">
                      <template slot-scope="scope">
                        <dict-tag :options="dict.type.YesOrNo" :value="scope.row.mandatory?'Y':'N'"/>
                      </template>
                    </el-table-column>
                    <el-table-column label="可用值"
                                    align="center"
                                    prop="options">
                      <template slot-scope="scope" v-if="scope.row.options!=null">
                        <span v-for="(item, index) in Object.keys(scope.row.options)" :key="item">
                          <span v-if="index>0">，</span><span style="color:#1890ff">{{ item }}：</span>{{ scope.row.options[item] }} 
                        </span>
                      </template>
                    </el-table-column>
                    <el-table-column label="描述"
                                    align="left"
                                    prop="usage" />
                  </el-table>
                </el-descriptions-item>
              </el-descriptions>
            </el-row>
          </template>
        </el-table-column>
        <el-table-column 
          label="名称"
          align="left"
          width="255"
          prop="name" />
        <el-table-column
          label="标签"
          width="255"
          prop="tagName">
          <template slot-scope="scope">
            <el-link type="primary">
              <span v-html="'<@'+scope.row.tagName+'>'"></span>
            </el-link>
          </template>
        </el-table-column>
        <el-table-column
          label="描述"
          align="left"
          prop="description">
        </el-table-column>
      </el-table>
    </el-row>
  </div>
</template>
<style>
</style>
<script>
import { getTagList } from "@/api/contentcore/staticize";

export default {
  name: "CMSTemlateTag",
  dicts: ['YesOrNo'],
  data () {
    return {
      loading: false,
      defaultProps: {
        children: "children",
        label: "label"
      },
      sourceTagList: [],
      tagList: [],
      tagName: undefined
    };
  },
  created () {
    this.loadTagList();
  },
  methods: {
    loadTagList() {
      this.loading = true;
      getTagList().then(response => {
        this.tagList = response.data;
        this.sourceTagList = response.data;
        this.loading = false;
      });
    },
    handleQuery() {
      this.tagList = this.sourceTagList.filter(data => {
        if (!this.tagName) {
          return true;
        }
        if (this.tagName && this.tagName.length > 0 && !data.name.toLowerCase().includes(this.tagName.toLowerCase()) && !data.tagName.toLowerCase().includes(this.tagName.toLowerCase())) {
          return false;
        }
        return true;
      });
    },
    handleRowClick(row) {
      this.$refs.tagListTable.toggleRowExpansion(row);
    }
  }
};
</script>