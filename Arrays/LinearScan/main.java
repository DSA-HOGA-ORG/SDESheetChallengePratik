package Arrays.LinearScan;

class Solution {
    public int majorityElement(int[] nums) {
        int cnt = 0;
        int n = nums.length;
        for(int i = 0; i < nums.length; i++) {
            cnt = 0;
            for(int j = 0; j < nums.length; j++) {
                if(nums[i] == nums[j]) cnt++;

            }
            if(cnt > n/2) return nums[i];
        }

        return -1;
    }
}