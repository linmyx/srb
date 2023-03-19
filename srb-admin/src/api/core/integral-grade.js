import request from '@/utils/request'

export default{
    getList(){
        return request({
            url:'/admin/core/integralGrade/getIntegralGradeList',
            method: 'GET'
        }) 
    },
    removeById(id){
        return request({
            url:`/admin/core/integralGrade/removeById/${id}`,
            method: 'Delete'
        }) 
    },
    saveIntegralGrade(integralGrade){
        return request({
            url:`/admin/core/integralGrade/save`,
            method: 'POST',
            data: integralGrade
        }) 
    },
    getById(id){
        return request({
            url:`/admin/core/integralGrade/getInfoById/${id}`,
            method: 'GET',
        })
    },
    updateIntegralGrade(integralGrade){
        return request({
            url:`/admin/core/integralGrade/updateIntegralGrade`,
            method: 'put',
            data:integralGrade
        })
    }
}