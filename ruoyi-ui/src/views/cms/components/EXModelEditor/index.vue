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
                 clearable 
                 placeholder="请选择">
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
        value-format="yyyy-MM-ddTHH:mm:ss"
        placeholder="选择日期">
      </el-date-picker>
      <el-date-picker v-if="field.controlType==='datetime'" 
        v-model="field.value"
        type="datetime"
        value-format="yyyy-MM-ddTHH:mm:ss"
        placeholder="选择日期时间">
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
    pk: {
      type: String,
      required: false
    }
  },
  watch: {
    pk(newVal) {
      this.pkValue = newVal;
    },
    pkValue(newVal) {
      if (newVal && newVal != null && newVal.length > 0) {
        this.loadModelFieldData();
      }
    },
  },
  data () {
    return {
      modelId: this.xmodel,
      pkValue: this.pk,
      fieldList: [],
    };
  },
  created() {
    this.loadModelFieldData();
  },
  methods: {
    loadModelFieldData() {
      getXModelFieldData(this.modelId, this.pkValue).then(response => {
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