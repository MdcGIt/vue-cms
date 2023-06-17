<template>
  <div class="app-container" style="padding: 0;">
    <el-form-item v-for="field in fieldList" 
                  :key="field.fieldName"
                  :label="field.label"
                  :prop="field.fieldName">
      <el-input v-if="field.controlType==='input'" 
                v-model="field.value" />
      <el-input v-if="field.controlType==='textarea'" 
                v-model="field.value"
                type="textarea" />
      <el-radio-group v-if="field.controlType==='radio'" 
                      v-model="field.value">
        <el-radio v-for="item in field.options" 
                  :key="item.value"
                  :label="item.value">{{ item.name }}</el-radio>
      </el-radio-group>
      <el-checkbox-group v-if="field.controlType==='checkbox'" 
                         v-model="field.value">
        <el-checkbox v-for="item in field.options" 
                     :key="item.value"
                     :label="item.value">{{ item.name }}</el-checkbox>
      </el-checkbox-group>
      <el-select v-if="field.controlType==='select'" 
                 v-model="field.value" 
                 clearable>
        <el-option
          v-for="item in field.options"
          :key="item.value"
          :label="item.name"
          :value="item.value">
        </el-option>
      </el-select>
      <el-date-picker v-if="field.controlType==='date'" 
        v-model="field.value"
        type="date"
        value-format="yyyy-MM-ddTHH:mm:ss">
      </el-date-picker>
      <el-date-picker v-if="field.controlType==='datetime'" 
        v-model="field.value"
        type="datetime"
        value-format="yyyy-MM-ddTHH:mm:ss">
      </el-date-picker>
    </el-form-item>
  </div>
</template>
<script>
import { getXModelFieldData } from "@/api/contentcore/exmodel";

export default {
  name: "CMSEXModelEditor",
  props: {
    xmodel: {
      type: String,
      required: true,
    },
    type: {
      type: String,
      required: true
    },
    id: {
      type: String,
      required: false
    }
  },
  watch: {
    type(newVal) {
      console.log("type", newVal)
      this.dataType = newVal;
    },
    id(newVal) {
      console.log("id", newVal)
      this.dataId = newVal;
    },
    dataId(newVal) {
      console.log("dataId", newVal)
      if (newVal && newVal != null && newVal.length > 0) {
        this.loadModelFieldData();
      }
    },
  },
  data () {
    return {
      modelId: this.xmodel,
      dataId: this.id,
      dataType: this.type,
      fieldList: [],
    };
  },
  created() {
    this.loadModelFieldData();
  },
  methods: {
    loadModelFieldData() {
      getXModelFieldData(this.modelId, this.dataType, this.dataId).then(response => {
        this.fieldList = response.data;
      });
    },
    getDatas() {
      let fdatas = {};
      this.fieldList.forEach(f => {
        fdatas[f.fieldName] = f.value;
      })
      return fdatas;
    }
  }
};
</script>
<style scoped>
.el-form-item {
  margin-bottom: 12px;
  width: 460px;
}
</style>