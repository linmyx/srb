<template>
    <div class="app-container">
      <!--查询表单-->
      <el-form :inline="true" class="demo-form-inline">
        <el-form-item label="手机号">
          <el-input v-model="searchObj.mobile" placeholder="手机号" />
        </el-form-item>
  
        <el-form-item label="用户类型">
          <el-select v-model="searchObj.userType" placeholder="请选择" clearable>
            <el-option label="投资人" value="1" />
            <el-option label="借款人" value="2" />
          </el-select>
        </el-form-item>
  
        <el-form-item label="用户状态">
          <el-select v-model="searchObj.status" placeholder="请选择" clearable>
            <el-option label="正常" value="1" />
            <el-option label="锁定" value="0" />
          </el-select>
        </el-form-item>
  
        <el-button type="primary" icon="el-icon-search" @click="getUserInfoPage()">
          查询
        </el-button>
        <el-button type="default" @click="resetData()">清空</el-button>
      </el-form>
  
      <!-- 列表 -->
      <el-table :data="list" border stripe>
        <el-table-column label="#" width="50">
          <template slot-scope="scope">
            {{ (page - 1) * limit + scope.$index + 1 }}
          </template>
        </el-table-column>
  
        <el-table-column label="用户类型" width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.userType === 1" type="success" size="mini">
              投资人
            </el-tag>
            <el-tag
              v-else-if="scope.row.userType === 2"
              type="warning"
              size="mini"
            >
              借款人
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="mobile" label="手机号" />
        <el-table-column prop="name" label="用户姓名" />
        <el-table-column prop="idCard" label="身份证号" />
        <el-table-column prop="integral" label="用户积分" />
        <el-table-column prop="createTime" label="注册时间" width="100" />
        <el-table-column label="绑定状态" width="90">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.bindStatus === 0" type="warning" size="mini">
              未绑定
            </el-tag>
            <el-tag
              v-else-if="scope.row.bindStatus === 1"
              type="success"
              size="mini"
            >
              已绑定
            </el-tag>
            <el-tag v-else type="danger" size="mini">绑定失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="用户状态" width="90">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.status === 0" type="danger" size="mini">
              锁定
            </el-tag>
            <el-tag v-else type="success" size="mini">
              正常
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" width="200">
            <template slot-scope="scope">
                <el-button
                        v-if="scope.row.status == 1"
                        type="primary"
                        size="mini"
                        @click="lock(scope.row.id, 0)"
                        >
                    锁定
                </el-button>
                <el-button
                        v-else
                        type="danger"
                        size="mini"
                        @click="lock(scope.row.id, 1)"
                        >
                    解锁
                </el-button>

                <el-button
                        type="primary"
                        size="mini"
                        @click="showLoginRecord(scope.row.id)"
                        >
                    登录日志
                </el-button>
            </template>

        </el-table-column>
      </el-table>
      <!-- 分页组件 -->
      <el-pagination
        :current-page="page"
        :total="total"
        :page-size="limit"
        :page-sizes="[10, 20]"
        style="padding: 30px 0; "
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="changePageSize"
        @current-change="changeCurrentPage"
      />

      <!-- 用户登录日志 -->
    <el-dialog title="用户登录日志" :visible.sync="dialogTableVisible">
        <el-table :data="loginRecordList" border stripe>
            <el-table-column type="index" />
            <el-table-column prop="ip" label="IP" />
            <el-table-column prop="createTime" label="登录时间" />
        </el-table>
    </el-dialog>

    </div>
  </template>

<script>
import userInfoApi from '@/api/core/user-info'
    export default {
        data() {
            return {
                list: null, // 数据列表
                total: 0, // 数据库中的总记录数
                page: 1, // 默认页码
                limit: 10, // 每页记录数
                searchObj: {}, // 查询条件
                loginRecordList: [], //会员登录日志
                dialogTableVisible: false //对话框是否显示
            }
        },
        created() {
            this.getUserInfoPage()
        },

        methods: {
            getUserInfoPage(){
                userInfoApi.getUserList(this.searchObj,this.page,this.limit)
                .then(res =>{
                    this.list=res.data.row
                    this.total=res.data.total
                })
            },
            //修改每页显示的长度
            changePageSize(size){
                this.limit=size
                this.getUserInfoPage()
            },

            //修改页码
            changeCurrentPage(page){
                this.page=page
                this.getUserInfoPage()
            },

            //重置查询条件
            resetData(){
                this.searchObj={}
                this.getUserInfoPage()
            },
            
            lock(id,status){
                userInfoApi.lockStatus(id,status)
                .then(res=>{
                    this.$message.success(res.message)
                    this.getUserInfoPage()
                })
            },

            showLoginRecord(userId){
                this.dialogTableVisible=true
                userInfoApi.getUserLoginRecord(userId)
                .then(res=>{
                   this.loginRecordList=res.data.rws
                })
            }

        },
    }
</script>

<style>

</style>