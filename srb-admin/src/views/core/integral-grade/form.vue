<template>
  <div class="app-container">
    <!-- 输入表单 -->
    <el-form label-width="120px">
      <el-form-item label="借款额度">
        <el-input-number v-model="integralGrade.borrowAmount" :min="0" />
      </el-form-item>
      <el-form-item label="积分区间开始">
        <el-input-number v-model="integralGrade.integralStart" :min="0" />
      </el-form-item>
      <el-form-item label="积分区间结束">
        <el-input-number v-model="integralGrade.integralEnd" :min="0" />
      </el-form-item>
      <el-form-item>
        <el-button
          :disabled="saveBtnDisabled"
          type="primary"
          @click="savaOrUpdate()"
        >
          保存
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import integralGradeApi from '@/api/core/integral-grade';
  export default {
    data() {
      return {
        integralGrade:{},
        saveBtnDisabled: false // 保存按钮是否禁用，防止表单重复提交
      }
    },
    created() {
      if(this.$route.params.id){
        this.getInfoById(this.$route.params.id)
      }
    },
    methods: {
      savaOrUpdate() {
        this.saveBtnDisabled=true
        if(this.integralGrade.id){
          this.updateIntegralGrade()
        }else {
          this.saveIntegralGrade()
        }
      },
      saveIntegralGrade(){
        integralGradeApi.saveIntegralGrade(this.integralGrade)
        .then(res=>{
            this.$message({
            type: 'success',
            message: res.message
          })
         this.$router.push('/core/integral-grade/list')
        })
      },
      getInfoById(id) {
        integralGradeApi.getById(id)
        .then(res=>{
            this.integralGrade=res.data.info
        })
      },
      updateIntegralGrade(){
        integralGradeApi.updateIntegralGrade(this.integralGrade)
        .then(res => {
          this.$message({
              type: 'success',
              message: res.message
          })
          this.$router.push('/core/integral-grade/list')
        })
      }
    },
  }
</script>

<style>

</style>