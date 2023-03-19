import request from '@/utils/request'
export default {
   getUserList(userInfoQuery,page,limit) {
    return request({
      url: `/admin/core/userInfo/getUserInfoPageList/${page}/${limit}`,
      method: 'get',
      params: userInfoQuery
    })
  },

  lockStatus(id,status){
    return request({
        url:`/admin/core/userInfo/lock/${id}/${status}`,
        method:'put'
    })
  },

  getUserLoginRecord(userId){
    return request({
        url:`/admin/core/userLoginRecord/getUserLoginRecord/${userId}`,
        method:'get'
    })
  }

}