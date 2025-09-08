package com.guideon.document.service;

import com.guideon.document.domain.BusinessInfoVO;
import com.guideon.document.domain.IndustryRulesVO;
import com.guideon.document.domain.PolicyVO;
import com.guideon.document.dto.PolicyDTO;
import com.guideon.document.mapper.BusinessInfoMapper;
import com.guideon.document.mapper.IndustryRulesMapper;
import com.guideon.document.mapper.PolicyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class PolicyServiceImpl implements PolicyService {

    private final BusinessInfoMapper businessInfoMapper;
    private final PolicyMapper policyMapper;
    private final IndustryRulesMapper industryRulesMapper;

    @Override
    public boolean isSmallBusiness(Long businessId) {

        log.info("소상공인 판별 시작");

        BusinessInfoVO businessInfoVO = businessInfoMapper.selectByBusinessId(businessId);

        if (businessInfoVO == null) {
            throw new IllegalArgumentException("존재하지 않는 사업체 정보입니다. businessId: " + businessId);
        }

        // 업종 조건 조회
        IndustryRulesVO industryRule = industryRulesMapper.selectIndustryRuleByCode(businessInfoVO.getIndustryCode());

        if (industryRule == null) {
            throw new IllegalArgumentException("업종 조건을 찾을 수 없습니다. industryCode: " + businessInfoVO.getIndustryCode());
        }

        // 1. 상시근로자 수 체크
        if (businessInfoVO.getEmployees() >= industryRule.getEmployeeLimit()) {
            log.info("소상공인 자격 미달 - 상시근로자 수: {}명 (기준: {}명 미만)",
                    businessInfoVO.getEmployees(), industryRule.getEmployeeLimit());
            return false;
        }

        // 2. 연매출 체크
        if (industryRule.getMaxRevenue() != null &&
                businessInfoVO.getRevenue() >= industryRule.getMaxRevenue()) {
            log.info("소상공인 자격 미달 - 연매출: {}원 (기준: {}원 미만)",
                    businessInfoVO.getRevenue(), industryRule.getMaxRevenue());
            return false;
        }

        log.info("소상공인 자격 요건 충족 - 업종: {}, 직원: {}명, 매출: {}원",
                businessInfoVO.getIndustryCode(),
                businessInfoVO.getEmployees(),
                businessInfoVO.getRevenue());

        return true;
    }

    @Override
    public List<PolicyDTO> filterEligiblePolicies(Long businessId) {

        log.info("정책자금 필터링 시작: businessId={}", businessId);

        BusinessInfoVO businessInfoVO = businessInfoMapper.selectByBusinessId(businessId);

        if (businessInfoVO == null) {
            throw new IllegalArgumentException("존재하지 않는 사업체 정보입니다. businessId: " + businessId);
        }

        // 활성화된 정책자금 조회
        List<PolicyVO> activePolicies = policyMapper.selectActivePolicies();
        List<PolicyDTO> eligiblePolicies = new ArrayList<>();

        log.info("전체 활성 정책자금 수: {}", activePolicies.size());

        // 각 정책자금에 대해 자격 조건 체크
        for (PolicyVO policy : activePolicies) {

            // 1. 대출목적 매칭 (운전자금/시설자금)
            if (policy.getLoanPurpose() != null &&
                    !policy.getLoanPurpose().equals(businessInfoVO.getLoanPurpose())) {
                continue;
            }

            // 2. 업력 조건 체크
            if (policy.getMinBusinessPeriod() != null &&
                    businessInfoVO.getBusinessPeriod() < policy.getMinBusinessPeriod()) {
                continue;
            }

            if (policy.getMaxBusinessPeriod() != null &&
                    businessInfoVO.getBusinessPeriod() > policy.getMaxBusinessPeriod()) {
                continue;
            }

            // 3. 연매출 조건 체크
            if (policy.getMinRevenue() != null &&
                    businessInfoVO.getRevenue() < policy.getMinRevenue()) {
                continue;
            }

            if (policy.getMaxRevenue() != null &&
                    businessInfoVO.getRevenue() > policy.getMaxRevenue()) {
                continue;
            }

            // 모든 조건을 통과한 정책자금 추가
            eligiblePolicies.add(PolicyDTO.fromVO(policy));
            log.debug("적합한 정책자금: {}", policy.getPolicyName());
        }

        log.info("적합한 정책자금 수: {}", eligiblePolicies.size());

        return eligiblePolicies;
    }
}