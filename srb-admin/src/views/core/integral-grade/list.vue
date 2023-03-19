<template>
  <div class="app-container">
    <!-- 表格 -->
    <el-table :data="list" border stripe>
      <el-table-column type="index" width="50" />
      <el-table-column prop="borrowAmount" label="借款额度" />
      <el-table-column prop="integralStart" label="积分区间开始" />
      <el-table-column prop="integralEnd" label="积分区间结束" />
      <el-table-column label="操作" width="200" align="center">
            <template slot-scope="scope">
                <el-button
                        type="danger"
                        size="mini"
                        icon="el-icon-delete"
                        @click="removeById(scope.row.id)"
                        >
                    删除
                </el-button>
                <router-link :to="'/core/integral-grade/edit/' + scope.row.id" style="margin-right:5px;" >
                    <el-button type="primary" size="mini" icon="el-icon-edit">
                        修改
                    </el-button>
                </router-link>
            </template>
        </el-table-column>
    </el-table>
  </div>
</template>

<script>
import integralGradeApi from '@/api/core/integral-grade';
    export default {
        data() {
            return {
                list:[]
            }
        },
        created() {
            this.getInfo()
        }, 
        methods: {
            getInfo() {
                integralGradeApi.getList()
                .then(res=>{
                    this.list=res.data.list
                    console.log('数据:',res.data.list)
                })
            },
            removeById(id){
                this.$confirm('此操作将永久删除该记录, 是否继续?', '提示', {
                confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning'
                })
                .then(() => {
                    integralGradeApi.removeById(id).then(response => {
                        this.$message.success(response.message)
                        this.getInfo()
                    })
                })
                .catch(error => {
                    this.$message.info('取消删除')
                })
            }
        },
    }
</script>

<style>

</style>