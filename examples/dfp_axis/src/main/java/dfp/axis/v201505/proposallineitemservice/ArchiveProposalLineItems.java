// Copyright 2015 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package dfp.axis.v201505.proposallineitemservice;

import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.dfp.axis.factory.DfpServices;
import com.google.api.ads.dfp.axis.utils.v201505.StatementBuilder;
import com.google.api.ads.dfp.axis.v201505.ProposalLineItem;
import com.google.api.ads.dfp.axis.v201505.ProposalLineItemPage;
import com.google.api.ads.dfp.axis.v201505.ProposalLineItemServiceInterface;
import com.google.api.ads.dfp.axis.v201505.UpdateResult;
import com.google.api.ads.dfp.lib.client.DfpSession;
import com.google.api.client.auth.oauth2.Credential;

/**
 * This example archives a proposal line item. To determine which proposal line 
 * items exist, run GetAllProposalLineItems.java.
 *
 * Credentials and properties in {@code fromFile()} are pulled from the
 * "ads.properties" file. See README for more info.
 *
 * Tags: ProposalLineItemService.getProposalLineItemsByStatement
 * Tags: ProposalLineItemService.performProposalLineItemAction
 *
 * @author Nicholas Chen
 */
public class ArchiveProposalLineItems {

  // Set the ID of the proposal line item to archive.
  private static final String PROPOSAL_LINE_ITEM_ID = "INSERT_PROPOSAL_LINE_ITEM_ID_HERE";

  public static void runExample(DfpServices dfpServices, DfpSession session,
      long proposalLineItemId) throws Exception {
    // Get the ProposalLineItemService.
    ProposalLineItemServiceInterface proposalLineItemService =
        dfpServices.get(session, ProposalLineItemServiceInterface.class);

    // Create a statement to select a proposal line item.
    StatementBuilder statementBuilder = new StatementBuilder()
        .where("WHERE id = :id")
        .orderBy("id ASC")
        .limit(StatementBuilder.SUGGESTED_PAGE_LIMIT)
        .withBindVariableValue("id", proposalLineItemId);

    // Default for total result set size.
    int totalResultSetSize = 0;

    do {
      // Get proposal line items by statement.
      ProposalLineItemPage page = proposalLineItemService.getProposalLineItemsByStatement(
          statementBuilder.toStatement());

      if (page.getResults() != null) {
        totalResultSetSize = page.getTotalResultSetSize();
        int i = page.getStartIndex();
        for (ProposalLineItem proposalLineItem : page.getResults()) {
          System.out.printf(
              "%d) Proposal line item with ID \"%d\" will be archived.%n", i++,
              proposalLineItem.getId());
        }
      }

      statementBuilder.increaseOffsetBy(StatementBuilder.SUGGESTED_PAGE_LIMIT);
    } while (statementBuilder.getOffset() < totalResultSetSize);

    System.out.printf("Number of proposal line items to be archived: %d%n", totalResultSetSize);

    if (totalResultSetSize > 0) {
      // Remove limit and offset from statement.
      statementBuilder.removeLimitAndOffset();

      // Create action.
      com.google.api.ads.dfp.axis.v201505.ArchiveProposalLineItems action =
          new com.google.api.ads.dfp.axis.v201505.ArchiveProposalLineItems();

      // Perform action.
      UpdateResult result =
          proposalLineItemService.performProposalLineItemAction(
              action, statementBuilder.toStatement());

      if (result != null && result.getNumChanges() > 0) {
        System.out.printf("Number of proposal line items archived: %d%n", result.getNumChanges());
      } else {
        System.out.println("No proposal line items were archived.");
      }
    }
  }

  public static void main(String[] args) throws Exception {
    // Generate a refreshable OAuth2 credential.
    Credential oAuth2Credential = new OfflineCredentials.Builder()
        .forApi(Api.DFP)
        .fromFile()
        .build()
        .generateCredential();

    // Construct a DfpSession.
    DfpSession session = new DfpSession.Builder()
        .fromFile()
        .withOAuth2Credential(oAuth2Credential)
        .build();

    DfpServices dfpServices = new DfpServices();

    runExample(dfpServices, session, Long.parseLong(PROPOSAL_LINE_ITEM_ID));
  }
}
